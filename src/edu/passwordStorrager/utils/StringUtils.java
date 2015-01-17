package edu.passwordStorrager.utils;

import java.io.File;

public class StringUtils {
    public static String fixFolder(String folder) {
        if (folder.endsWith(File.separator)) {
            return folder;
        } else {
            return folder + File.separator;
        }
    }
}
