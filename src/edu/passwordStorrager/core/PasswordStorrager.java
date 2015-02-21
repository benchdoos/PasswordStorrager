package edu.passwordStorrager.core;

import edu.passwordStorrager.gui.AuthorizeDialog;
import edu.passwordStorrager.gui.MainForm;
import edu.passwordStorrager.objects.Key;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class PasswordStorrager {


    public final static File JAR_FILE = new File(PasswordStorrager.class.getProtectionDomain()
            .getCodeSource().getLocation().getPath());

    public static Key key = new Key();
    public static boolean isUnlocked = false;
    public static Properties propertiesApplication = new Properties();
    public static Properties propertiesFrames = new Properties();

    public static ArrayList<MainForm> framesMainForm = new ArrayList<>();
    public static ArrayList<AuthorizeDialog> framesAuthForm = new ArrayList<>();

    public static List<Window> frames = Collections.synchronizedList(new ArrayList<Window>());


    public static void main(String[] args) {
        //-Xdock:name=PasswordStorrager
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                createFolderForLog4J();
                new Core();
            }
        });
    }

    private static void createFolderForLog4J() {
        String tmp = Application.APPLICATION_TMP_FOLDER;
        System.setProperty(Application.APPLICATION_LOG_FOLDER_PROPERTY, Application.APPLICATION_LOG_FOLDER);

        String PS = tmp + Application.APPLICATION_FOLDER_NAME + File.separator;
        boolean status = false;
        if (!new File(PS).exists()) {
            new File(PS).mkdir();
            status = true;
        }
        String logsFolder = PS + "Logs" + File.separator;
        if (!new File(logsFolder).exists()) {
            new File(logsFolder).mkdir();
            status = true;
        }
        if (!new File(logsFolder + "DEBUG").exists()) {
            new File(logsFolder + "DEBUG").mkdir();
            status = true;
        }
        if (!new File(logsFolder + "INFO").exists()) {
            new File(logsFolder + "INFO").mkdir();
            status = true;
        }
        if (!new File(logsFolder + "WARN").exists()) {
            new File(logsFolder + "WARN").mkdir();
            status = true;
        }
        if (status) {
            System.out.println("########################################################");
            System.out.println("JAR_FILE: " + JAR_FILE);
            System.out.println("########################################################");
            try {
                Runtime.getRuntime().exec("java -jar " + JAR_FILE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
