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

    public static String parseUrl(String value) {
        if (!value.isEmpty()) {
            if (value.length() > 7) {
                if (!value.contains("http://")) {
                    value = "http://" + value;
                }
            } else {
                value = "http://" + value;
            }
        }
        return value;
    }


}
