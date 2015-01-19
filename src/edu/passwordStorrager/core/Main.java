package edu.passwordStorrager.core;

import edu.passwordStorrager.gui.AuthorizeDialog;
import edu.passwordStorrager.gui.FirstLaunchDialog;
import edu.passwordStorrager.objects.Key;

import javax.swing.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import static edu.passwordStorrager.utils.FileUtils.exists;

public class Main {
    public static final Properties system = System.getProperties();
    public static final String OS_NAME = system.getProperty("os.name");
    public static final String USER_HOME = system.getProperty("user.home");
    public static final boolean IS_MAC = OS_NAME.toLowerCase().equals("mac os x");
    public static final boolean IS_WINDOWS = OS_NAME.toLowerCase().equals("windows");


    public static Key key = new Key();
    public static Properties properties = new Properties();
    public static Properties frames = new Properties();


    Main() {
        new PropertiesManager(); //MUST BE CALLED
        
        printOSParameters();
        checkIfOsIsSupported();

        if (exists(PropertiesManager.framePropertiesFilePath)) {
            Main.frames = new Properties();
            try {
                Main.frames.load(new FileInputStream(PropertiesManager.framePropertiesFilePath));
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Can not load Frames properties");
            }
        }else {
            Main.frames = new Properties();
        }
        
        if (exists(PropertiesManager.propertiesFilePath)) {
            new AuthorizeDialog();
        } else {
            new FirstLaunchDialog();
        }
    }

    private void printOSParameters() {
        System.out.println("==========================System=========================");
        System.out.println("System:");
        System.out.println("\tOS: " + OS_NAME + " v." + system.getProperty("os.version") + " arch: " + system.getProperty("os.arch"));
        System.out.println("Java:");
        System.out.println("\tJava version: " + system.getProperty("java.specification.version") + "(" + system.getProperty("java.version") + ")");
        System.out.println("\t" + system.getProperty("java.runtime.name") + " v." + system.getProperty("java.vm.version"));
        System.out.println("User:");
        System.out.println("\tName: " + system.getProperty("user.name") + " Home: " + USER_HOME);
        System.out.println("\tTime zone: " + system.getProperty("user.timezone") + " (" + system.getProperty("user.country") + ") language: " + system.getProperty("user.language"));
        System.out.println("=========================================================");
    }

    public static void main(String[] args) {
        new Main();
    }

    private void checkIfOsIsSupported() {
        if (IS_MAC) {
            macOsHandler();
        } else if (IS_WINDOWS) {
            //osHandler here
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
