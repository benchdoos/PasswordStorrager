package edu.passwordStorrager.utils;

import edu.passwordStorrager.core.Main;

import java.io.IOException;

public class FileUtils {


    public static void setFileHidden(String filePath) {
        try {
            if (Main.isMac) {
                Runtime.getRuntime().exec("setfile -a V " + filePath);
                Runtime.getRuntime().exec("chflags hidden " + filePath);
            }

            if (Main.isWindows) {
                Runtime.getRuntime().exec("attrib +H " + filePath);
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Can not make file hidden");
        }
    }
}
