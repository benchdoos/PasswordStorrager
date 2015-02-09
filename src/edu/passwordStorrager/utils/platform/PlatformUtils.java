package edu.passwordStorrager.utils.platform;

import edu.passwordStorrager.core.Main;
import edu.passwordStorrager.utils.UnsupportedOsException;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;

import static edu.passwordStorrager.core.Main.*;
import static edu.passwordStorrager.utils.FrameUtils.getCurrentClassName;

public class PlatformUtils {
    public static Image appIcon = Toolkit.getDefaultToolkit()
            .getImage(getCurrentClassName().getClass().getResource("/resources/icons/icon_black_256.png"));
    private static final Logger log = Logger.getLogger(getCurrentClassName());

    public static void initializeOS() {
        log.info("System - OS: " + OS_NAME
                + " v" + system.getProperty("os.version")
                + " " + system.getProperty("os.arch")
                + "; Java v" + system.getProperty("java.version")
                + "; Program v" + Main.version);
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
        System.out.println("\tOS: " + OS_NAME + " v" + system.getProperty("os.version") + " arch: " + system.getProperty("os.arch"));
        System.out.println("Java:");
        System.out.println("\tJava version: " + system.getProperty("java.specification.version") + "(" + system.getProperty("java.version") + ")");
        System.out.println("\t" + system.getProperty("java.runtime.name") + " v" + system.getProperty("java.vm.version"));
        System.out.println("User:");
        System.out.println("\tName: " + system.getProperty("user.name") + " Home: " + USER_HOME);
        System.out.println("\tTime zone: " + system.getProperty("user.timezone") + " (" + system.getProperty("user.country") + ") language: " + system.getProperty("user.language"));
        System.out.println("Logging to: " + system.getProperty("java.io.tmpdir") + "PasswordStorrager/Logs/");
        System.out.println("=========================================================");
    }
}
