package edu.passwordStorrager.protector;

import edu.passwordStorrager.core.Application;

import java.io.File;

public interface Values {
    String DEFAULT_PROPERTIES_FILE_NAME = "storage.properties";
    String DEFAULT_KEY_FILE_NAME = ".key";
    String DEFAULT_STORAGE_FILE_NAME = "storage";
    String DEFAULT_FRAME_PROPERTIES_FILE_NAME = "frames.properties";
    int DEFAULT_LOCK_DELAY = 60 * 1000;

    String DEFAULT_MAC_PROPERTIES_FILE_FOLDER = Application.USER_HOME + File.separator + "Library" + File.separator +
            "Application Support" + File.separator + Application.APPLICATION_FOLDER_NAME + File.separator;

    String DEFAULT_WINDOWS_PROPERTIES_FILE_FOLDER = Application.USER_HOME + File.separator + "AppData" + File.separator +
            "Local" + File.separator + Application.APPLICATION_FOLDER_NAME + File.separator;
    String DEFAULT_UNIX_PROPERTIES_FILE_FOLDER = Application.USER_HOME + File.separator + ".config" + File.separator + Application.APPLICATION_FOLDER_NAME + File.separator;
}
