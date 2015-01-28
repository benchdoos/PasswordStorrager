package edu.passwordStorrager.cloud;

import com.dropbox.core.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class DropBoxManager {
    String storageFilePath;
    public DropBoxManager(String storageFilePath) {
        this.storageFilePath = storageFilePath;
    }
    
    protected void syncDropBox() {
        File file = new File(storageFilePath);
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        long local = file.lastModified();
//        System.out.println("Before Format : " + file.lastModified());
//        System.out.println("After Format : " + sdf.format(file.lastModified()));
        try {
            firstAuthorize();
        } catch (IOException | DbxException e) {
            e.printStackTrace();
        }

    }

    public void firstAuthorize() throws IOException, DbxException {
        final String APP_KEY = "s1f7xpeaot64kee";
        final String APP_SECRET = "bwtnqhj92ba3v3y";

        DbxAppInfo appInfo = new DbxAppInfo(APP_KEY, APP_SECRET);

        DbxRequestConfig config = new DbxRequestConfig(
                "JavaTutorial/1.0", Locale.getDefault().toString());
        DbxWebAuthNoRedirect webAuth = new DbxWebAuthNoRedirect(config, appInfo);

        String userLocale = "";
        DbxRequestConfig requestConfig = new DbxRequestConfig("text-edit/0.1", userLocale);
        //DbxAppInfo appInfo = DbxAppInfo.Reader.readFromFile("api.app");
        //DbxWebAuthNoRedirect webAuth = new DbxWebAuthNoRedirect(requestConfig, appInfo);

        String authorizeUrl = webAuth.start();
        System.out.println("1. Go to " + authorizeUrl);
        System.out.println("2. Click \"Allow\" (you might have to log in first).");
        System.out.println("3. Copy the authorization code.");
        System.out.print("Enter the authorization code here: ");

        String code = new BufferedReader(new InputStreamReader(System.in)).readLine();
        if (code == null) return;
        code = code.trim();

        DbxAuthFinish authFinish = webAuth.finish(code);
        String accessToken = authFinish.accessToken;

        DbxClient client = new DbxClient(requestConfig, accessToken);

        System.out.println("Linked account: " + client.getAccountInfo().displayName);
    }
}
