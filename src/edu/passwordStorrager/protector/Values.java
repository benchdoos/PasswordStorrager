package edu.passwordStorrager.protector;

import edu.passwordStorrager.core.Main;

import java.io.File;

public interface Values {
    String DEFAULT_PROPERTIES_FILE_NAME = "storage.properties";
    String DEFAULT_KEY_FILE_NAME = ".key";
    String DEFAULT_STORAGE_FILE_NAME = "storage";

    String DEFAULT_MAC_PROPERTIES_FILE_FOLDER = Main.userHome + File.separator + "Library" + File.separator +
            "Application Support" + File.separator + "PasswordStorrager" + File.separator;

    String DEFAULT_WINDOWS_PROPERTIES_FILE_FOLDER = Main.userHome + File.separator + "AppData" + File.separator +
            "Local" + File.separator + "PasswordStorrager" + File.separator;
}
