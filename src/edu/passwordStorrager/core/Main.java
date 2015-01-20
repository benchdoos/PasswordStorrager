package edu.passwordStorrager.core;

import edu.passwordStorrager.gui.AuthorizeDialog;
import edu.passwordStorrager.gui.FirstLaunchDialog;
import edu.passwordStorrager.objects.Key;
import edu.passwordStorrager.utils.UnsupportedOsException;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import static edu.passwordStorrager.utils.FileUtils.exists;
import static edu.passwordStorrager.utils.FrameUtils.getCurrentClassName;

public class Main extends JFrame{
    private static final Logger log = Logger.getLogger(getCurrentClassName());
    
    public static final Properties system = System.getProperties();
    public static final String OS_NAME = system.getProperty("os.name");
    public static final String USER_HOME = system.getProperty("user.home");
    public static final boolean IS_MAC = OS_NAME.toLowerCase().equals("mac os x");
    public static final boolean IS_WINDOWS = OS_NAME.toLowerCase().equals("windows");


    public static Key key = new Key();
    public static Properties properties = new Properties();
    public static Properties frames = new Properties();



    Main() {
        log.debug("Start");

        initSystem();

        if (exists(PropertiesManager.framePropertiesFilePath)) {
            Main.frames = new Properties();
            try {
                Main.frames.load(new FileInputStream(PropertiesManager.framePropertiesFilePath));
                log.debug("Frames properties loaded from " + PropertiesManager.framePropertiesFilePath);
            } catch (IOException e) {
                log.warn("Can not load Frames properties", e);
            }
        } else {
            Main.frames = new Properties();
            log.debug("Creating new Frames properties");
        }

        if (exists(PropertiesManager.propertiesFilePath)) {
            log.debug("Properties file found, loading AuthorizeDialog");
            new AuthorizeDialog();
        } else {
            log.debug("Properties file not found, loading FirstLaunchDialog");
            new FirstLaunchDialog();
        }
    }

    private void initSystem() {
        new PropertiesManager(); //MUST BE CALLED. DO NOT TOUCH
        
        printOSParameters();
        if(isOsSupported()) {
            initOs();
        } else{
            throw new UnsupportedOsException();
        }
    }

    public static boolean isOsSupported() {
        return IS_MAC;
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
        System.out.println("Logging to: " + system.getProperty("java.io.tmpdir") + "PasswordStorrager/Logs/");
        System.out.println("=========================================================");
    }

    public static void main(String[] args) {
        new Main();
    }

    private void initOs() {
        log.info("System - OS: " + OS_NAME + " v." + system.getProperty("os.version") + " " + system.getProperty("os.arch") + "; Java v." + system.getProperty("java.version"));
        
        if (!isOsSupported()) {
            throw new UnsupportedOsException();
        }

        if (IS_MAC) {
            initializeMacOsX();
        } else if (IS_WINDOWS) {
            //osHandler here
        }
    }
    
    private void initializeMacOsX() {
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
    
    
    public static void onExit() {
        log.debug("Stop");
    }
}
