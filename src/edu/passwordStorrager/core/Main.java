package edu.passwordStorrager.core;

import edu.passwordStorrager.gui.AuthorizeDialog;
import edu.passwordStorrager.gui.FirstLaunchDialog;
import edu.passwordStorrager.objects.Key;

import javax.swing.*;
import java.util.Properties;

public class Main {
    public static final Properties system = System.getProperties();
    public static final String osName = system.getProperty("os.name");
    public static final String userHome = system.getProperty("user.home");
    public static final boolean isMac = osName.toLowerCase().equals("mac os x");
    public static final boolean isWindows = osName.toLowerCase().equals("windows");


    public static Key key;
    public static Properties properties = new Properties();


    Main() {
        macOSHandler();
        Main.key = new Key();
        if (PropertiesManager.exists()) {
            new AuthorizeDialog();
        } else {
            new FirstLaunchDialog();
        }
        //new AuthorizeDialog();
        //new Encryption();
    }

    public static void main(String[] args) {
        new Main();
    }

    public static void showProperties(Properties properties) {
        for (String key : properties.stringPropertyNames()) {
            String value = properties.getProperty(key);
            System.out.println(key + " => " + value);
        }
    }

    private void macOSHandler() {
        if (System.getProperty("os.name").toLowerCase().equals("mac os x")) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "PasswordStorrager");
            System.setProperty("apple.awt.fileDialogForDirectories", "true");
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | InstantiationException | UnsupportedLookAndFeelException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
