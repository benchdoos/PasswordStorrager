package edu.passwordStorrager.utils;

import edu.passwordStorrager.core.Main;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;

import static edu.passwordStorrager.utils.FrameUtils.getCurrentClassName;

public class FileUtils {
    private static final Logger log = Logger.getLogger(getCurrentClassName());


    /**
     * Set File on file system hidden.
     *
     * @param file <code>File</code> that needs to be hidden.
     */
    public static void setFileHidden(File file) {
        setFileHidden(file.getAbsolutePath());
    }

    /**
     * Set <code>File</code> on file system hidden.
     *
     * @param filePath Path to <code>File</code> that needs to be hidden.
     */
    public static void setFileHidden(String filePath) {
        try {
            if (Main.IS_MAC) {
                Runtime.getRuntime().exec("setfile -a V " + filePath); //not sure if it works when xcode not installed
                Runtime.getRuntime().exec("chflags hidden " + filePath);
            }

            if (Main.IS_WINDOWS) {
                Runtime.getRuntime().exec("attrib +H " + filePath);
            }
        } catch (IOException e) {
            log.warn("Can not make file hidden : " + filePath, e);
        }
    }

    public static boolean exists(String filePath) {
        return (Main.IS_MAC || Main.IS_WINDOWS) && new File(filePath).exists();
    }
}
