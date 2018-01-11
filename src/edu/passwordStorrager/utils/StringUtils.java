package edu.passwordStorrager.utils;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import static edu.passwordStorrager.utils.FrameUtils.getCurrentClassName;

public class StringUtils {
    private static final Logger log = Logger.getLogger(getCurrentClassName());

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

    public static boolean isUrl(String value) {

        return value.contains("ru")
                || value.contains("com")
                || value.contains("org")
                || value.contains("ua")
                || value.contains("net")
                || value.contains("su")
                || value.contains("fm")
                || value.contains("fy")
                || value.contains("info");
    }

    public static void openWebPage(String string) {
        try {
            java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
            if (StringUtils.isUrl(string)) {
                desktop.browse(URI.create(StringUtils.parseUrl(string)));
            }
        } catch (IOException e) {
            log.warn("Can not open in browser: " + StringUtils.parseUrl(string));
        }
    }

}
