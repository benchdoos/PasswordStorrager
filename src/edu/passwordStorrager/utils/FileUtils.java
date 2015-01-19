package edu.passwordStorrager.utils;

import edu.passwordStorrager.core.Main;

import java.io.IOException;

public class FileUtils {


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
}
