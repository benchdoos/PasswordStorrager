package edu.passwordStorrager.utils.platform;

import edu.passwordStorrager.core.Application;
import edu.passwordStorrager.utils.UnsupportedOsException;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;

import static edu.passwordStorrager.core.Application.*;
import static edu.passwordStorrager.utils.FrameUtils.getCurrentClassName;

public class PlatformUtils {
    public static Image appIcon = Toolkit.getDefaultToolkit()
            .getImage(getCurrentClassName().getClass().getResource("/resources/icons/icon_black_256.png"));
    private static final Logger log = Logger.getLogger(getCurrentClassName());

    public static void initializeOS() {
        log.info("System - OS: " + OS_NAME
                + " v" + OS_VERSION
                + " " + OS_ARCH
                + "; Java v" + JAVA_VERSION
                + "; Program v" + Application.APPLICATION_VERSION);
        if (!PlatformUtils.isOsSupported()) {
            throw new UnsupportedOsException();
        }

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | UnsupportedLookAndFeelException | IllegalAccessException e) {
            e.printStackTrace();
        }

        if (IS_MAC) {
            MacOsXUtils.initializeMacOSX();
        } else if (IS_WINDOWS) {
            //osHandler here
        }
    }

    public static boolean isOsSupported() {
        System.out.println("supported: " + (IS_MAC || IS_WINDOWS));
        return IS_MAC || IS_WINDOWS;
    }


    public static void printOSParameters() {
        System.out.println("==========================System=========================");
        System.out.println("System:");
        System.out.println("\tOS: " + OS_NAME + " v" + OS_VERSION + " arch: " + OS_ARCH);
        System.out.println("Java:");
        System.out.println("\tJava version: " + SYSTEM.getProperty("java.specification.version") + "(" + JAVA_VERSION + ")");
        System.out.println("\t" + SYSTEM.getProperty("java.runtime.name") + " v" + SYSTEM.getProperty("java.vm.version"));
        System.out.println("User:");
        System.out.println("\tName: " + USER_NAME + " Home: " + USER_HOME);
        System.out.println("\tTime zone: " + SYSTEM.getProperty("user.timezone") + " (" + SYSTEM.getProperty("user.country") + ") language: " + SYSTEM.getProperty("user.language"));
        System.out.println("Logging to: " + SYSTEM.get(APPLICATION_LOG_FOLDER_PROPERTY));
        System.out.println("=========================================================");
    }
}
