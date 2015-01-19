package edu.passwordStorrager.core;

import edu.passwordStorrager.gui.AuthorizeDialog;
import edu.passwordStorrager.gui.FirstLaunchDialog;
import edu.passwordStorrager.objects.Key;

import javax.swing.*;
import java.util.Properties;

public class Main {
    public static final Properties system = System.getProperties();
    public static final String OS_NAME = system.getProperty("os.name");
    public static final String USER_HOME = system.getProperty("user.home");
    public static final boolean IS_MAC = OS_NAME.toLowerCase().equals("mac os x");
    public static final boolean IS_WINDOWS = OS_NAME.toLowerCase().equals("windows");


    public static Key key;
    public static Properties properties = new Properties();


    Main() {
        checkIfOsIsSupported();
        
        Main.key = new Key();

        new PropertiesManager(); //MUST BE CALLED

        if (PropertiesManager.exists()) {
            new AuthorizeDialog();
        } else {
            new FirstLaunchDialog();
        }
    }

    public static void main(String[] args) {
        new Main();
    }

    private void checkIfOsIsSupported() {
        if (IS_MAC) {
            macOsHandler();
        } else if (IS_WINDOWS) {
            //osHandler here
            System.out.println("Windows");
            throw new UnsupportedOperationException("This OS is not supported yet : " + OS_NAME);
        } else {
            throw new UnsupportedOperationException("This OS is not supported yet : " + OS_NAME);
        }
    }

    private void macOsHandler() {
        if (IS_MAC) {
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
