package edu.passwordStorrager.core;

import edu.passwordStorrager.gui.AuthorizeDialog;
import edu.passwordStorrager.gui.FirstLaunchDialog;
import edu.passwordStorrager.gui.MainForm;
import edu.passwordStorrager.utils.platform.PlatformUtils;
import org.apache.log4j.Logger;

import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ConcurrentModificationException;
import java.util.Properties;

import static edu.passwordStorrager.utils.FileUtils.exists;
import static edu.passwordStorrager.utils.FrameUtils.getCurrentClassName;

public class Core implements Application {
    private static final Logger log = Logger.getLogger(getCurrentClassName());

    private static boolean isExitCanceled = false;

    public Core() {
        log.debug("Launch");

        initSystem();

        if (exists(PropertiesManager.framePropertiesFilePath)) {
            PasswordStorrager.propertiesFrames = new Properties();
            try {
                PasswordStorrager.propertiesFrames.load(new FileInputStream(PropertiesManager.framePropertiesFilePath));
                log.debug("Frames properties loaded from " + PropertiesManager.framePropertiesFilePath);
            } catch (IOException e) {
                log.warn("Can not load Frames properties", e);
            }
        } else {
            PasswordStorrager.propertiesFrames = new Properties();
            log.debug("Creating new Frames properties");
        }

        ////////////////////////////////////////////

        if (exists(PropertiesManager.propertiesFilePath)) {
            log.debug("Properties file found, loading AuthorizeDialog");
            new AuthorizeDialog(true);
        } else {
            log.debug("Properties file not found, loading FirstLaunchDialog");
            new FirstLaunchDialog();
        }
    }

    public static boolean isIsExitCanceled() {
        return isExitCanceled;
    }

    public static void setIsExitCanceled(boolean isExitCanceled) {
        Core.isExitCanceled = isExitCanceled;
    }

    private void initSystem() {
        new PropertiesManager(); //MUST BE CALLED. DO NOT TOUCH

        System.out.println(PlatformUtils.getSystemParameters());
        PlatformUtils.initializeOS();
    }

    public static void onQuit() {
        disposeFrames();

        //TODO sync here
        if (IS_MAC) { //TODO return when multi-window ready (FrameUtils too)
            if (!isExitCanceled) {
                log.debug("Quit");
                System.exit(0);
            }
        } else {
            log.debug("Quit");
            System.exit(0);
        }

        /*log.debug("Quit");
        System.exit(0);*/

    }

    /**
     * Disposes MainForm firstly
     */
    private static void disposeFrames() {
        try {
            disposeMainFrames();
        } catch (ConcurrentModificationException e) {
            disposeMainFrames();
        }

        try {
            disposeWindows();
        } catch (ConcurrentModificationException e) {
            disposeWindows();
        }
    }

    private static void disposeWindows() {
        for (Window w : PasswordStorrager.frames) {
            w.dispose();
        }
    }

    private static void disposeMainFrames() {
        for (Window w : PasswordStorrager.frames) {
            if (w instanceof MainForm) {
                MainForm mf = (MainForm) w;
                mf.dispose();
            }
        }
    }
}
