package edu.passwordStorrager.core;

import edu.passwordStorrager.gui.AuthorizeDialog;
import edu.passwordStorrager.gui.MainForm;
import edu.passwordStorrager.objects.Key;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

public class Main {


    private final static File JAR_FILE = new File(Main.class.getProtectionDomain()
            .getCodeSource().getLocation().getPath());

    public static final String version = "0.1.b";

    public static final Properties system = System.getProperties();
    public static final String OS_NAME = system.getProperty("os.name");
    public static final String USER_HOME = system.getProperty("user.home");
    public static final boolean IS_MAC = OS_NAME.toLowerCase().contains("mac");
    public static final boolean IS_WINDOWS = OS_NAME.toLowerCase().contains("windows");


    public static Key key = new Key();
    public static boolean isAuthorized = false;
    public static Properties propertiesApplication = new Properties();
    public static Properties propertiesFrames = new Properties();

    public static ArrayList<MainForm> framesMainForm = new ArrayList<MainForm>();
    public static ArrayList<AuthorizeDialog> framesAuthForm = new ArrayList<AuthorizeDialog>();

    public static ArrayList<Window> frames = new ArrayList<Window>();

    Main() {
        new Core();
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                createFolderForLog4J();
                new Main();
            }
        });
    }

    private static void createFolderForLog4J() {
        String tmp = System.getProperty("java.io.tmpdir");
        String PS = tmp + "PasswordStorrager" + File.separator;
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
