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
        if (isMac) {
            macOSHandler();
        }
        Main.key = new Key();
        if (PropertiesManager.exists()) {
            new AuthorizeDialog();
        } else {
            new FirstLaunchDialog();
        }
    }

    public static void main(String[] args) {
        new Main();
    }

    private void macOSHandler() {
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
