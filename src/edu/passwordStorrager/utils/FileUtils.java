package edu.passwordStorrager.utils;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;

import static edu.passwordStorrager.core.Application.IS_MAC;
import static edu.passwordStorrager.core.Application.IS_WINDOWS;
import static edu.passwordStorrager.utils.FrameUtils.getCurrentClassName;

public class FileUtils {
    private static final Logger log = Logger.getLogger(getCurrentClassName());


    /**
     * Set File on file SYSTEM hidden.
     *
     * @param file <code>File</code> that needs to be hidden.
     */
    public static void setFileHidden(File file) {
        setFileHidden(file.getAbsolutePath());
    }

    /**
     * Set <code>File</code> on file SYSTEM hidden.
     *
     * @param filePath Path to <code>File</code> that needs to be hidden.
     */
    public static void setFileHidden(String filePath) {
        try {
            if (IS_MAC) {
                Runtime.getRuntime().exec("setfile -a V " + filePath);
                //Runtime.getRuntime().exec("chflags hidden " + filePath);
            }

            if (IS_WINDOWS) {
                Runtime.getRuntime().exec("attrib +H " + filePath);
            }
        } catch (IOException e) {
            log.warn("Can not make file hidden : " + filePath, e);
        }
    }

    public static boolean exists(String filePath) {
        return new File(filePath).exists();
    }

    public static boolean validPath(String path) {
        return new File(path).exists() && new File(path).isDirectory();
    }
}
