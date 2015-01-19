package edu.passwordStorrager.utils;

import edu.passwordStorrager.core.Main;

import java.io.File;
import java.io.IOException;

public class FileUtils {


    /**
     * Set File on file system hidden.
     * @param file <code>File</code> that needs to be hidden.
     */
    public static void setFileHidden(File file) {
        setFileHidden(file.getAbsolutePath());
    }

    /**
     * Set <code>File</code> on file system hidden.
     * @param filePath Path to <code>File</code> that needs to be hidden.
     */
    public static void setFileHidden(String filePath) {
        try {
            if (Main.IS_MAC) {
                Runtime.getRuntime().exec("setfile -a V " + filePath);
                Runtime.getRuntime().exec("chflags hidden " + filePath);
            }

            if (Main.IS_WINDOWS) {
                Runtime.getRuntime().exec("attrib +H " + filePath);
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Can not make file hidden");
        }
    }

    public static boolean exists(String filePath) {
        return (Main.IS_MAC || Main.IS_WINDOWS) && new File(filePath).exists();
    }
}
