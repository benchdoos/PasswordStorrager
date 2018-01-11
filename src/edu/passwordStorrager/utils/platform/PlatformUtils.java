package edu.passwordStorrager.utils.platform;

import edu.passwordStorrager.core.Application;
import org.apache.log4j.Logger;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;

import javax.swing.*;
import java.awt.*;

import static edu.passwordStorrager.core.Application.*;
import static edu.passwordStorrager.utils.FrameUtils.getCurrentClassName;

public class PlatformUtils {
    public static Image appIcon = Toolkit.getDefaultToolkit()
            .getImage(PlatformUtils.class.getResource("/resources/icons/icon_black_256.png"));
    private static final Logger log = Logger.getLogger(getCurrentClassName());

    public static void initializeOS() {
        /*log.info("System - OS: " + OS_NAME
                + " v" + OS_VERSION
                + " " + OS_ARCH
                + "; Java v" + JAVA_VERSION
                + "; Program v" + Application.APPLICATION_VERSION);*/

        SystemInfo si = new SystemInfo();
        OperatingSystem os = si.getOperatingSystem();
        HardwareAbstractionLayer hal = si.getHardware();

        StringBuilder sb = new StringBuilder();
        sb.append("System: ");
        sb.append(os).append(" arch: ").append(OS_ARCH);
        sb.append(" ");
        sb.append("Core: ").append(hal.getProcessor().getName());
        sb.append(" ");
        sb.append("Memory: ").append(hal.getMemory().getAvailable() / (1024 * 1024))
                .append("MB (total:").append(hal.getMemory().getTotal() / (1024 * 1024)).append(" MB)");
        sb.append("; ");
        sb.append("Java v").append(JAVA_VERSION).append("; Program v" + Application.APPLICATION_VERSION);

        log.info(sb.toString());

        if (!PlatformUtils.isOsSupported()) {
            throw new UnsupportedOsException();
        }

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | UnsupportedLookAndFeelException | IllegalAccessException e) {
            e.printStackTrace();
        }

        if (IS_MAC) {
            //TODO fix in windows
            MacOsXUtils.initializeMacOSX();
        } else if (IS_WINDOWS) {
            //osHandler here
        }
    }

    public static boolean isOsSupported() {
        System.out.println("supported: " + (IS_MAC || IS_WINDOWS));
        return IS_MAC || IS_WINDOWS || IS_UNIX;
    }


    public static String getSystemParameters() {
        SystemInfo si = new SystemInfo();
        OperatingSystem os = si.getOperatingSystem();
        HardwareAbstractionLayer hal = si.getHardware();
        int mb = 1024 * 1024;

        StringBuilder sb = new StringBuilder();

        sb.append("==========================System=========================").append("\n");
        sb.append("System:").append("\n");
        sb.append("\tOS: ").append(os.getManufacturer()).append(" ").append(os.getFamily()).append("\n");
        sb.append("\tVersion: ").append(os.getVersion()).append("\n");
        sb.append("\tArch: ").append(OS_ARCH).append("\n");

        sb.append("Hardware:").append("\n");
        sb.append("\tCore: ").append(hal.getProcessor()).append(" (total:").append(hal.getProcessor()).append(")").append("\n");
        sb.append("\tMemory: ").append(hal.getMemory().getAvailable() / mb).append(" (total:").append(hal.getMemory().getTotal() / mb).append(") MB").append("\n");

        sb.append("Java:").append("\n");
        sb.append("\tJava version: ").append(SYSTEM.getProperty("java.specification.version")).append("(").append(JAVA_VERSION).append(")").append("\n");
        sb.append("\t").append(SYSTEM.getProperty("java.runtime.name")).append(" v").append(SYSTEM.getProperty("java.vm.version")).append("\n");

        sb.append("User:").append("\n");
        sb.append("\tName: ").append(USER_NAME).append(" Home: ").append(USER_HOME).append("\n");
        sb.append("\tTime zone: ").append(SYSTEM.getProperty("user.timezone")).append(" (").append(SYSTEM.getProperty("user.country")).append(") language: ").append(SYSTEM.getProperty("user.language")).append("\n");

        sb.append("Logging to: ").append(SYSTEM.get(APPLICATION_LOG_FOLDER_PROPERTY)).append("\n");
        sb.append("=========================================================").append("\n");

        return sb.toString();
    }
}
