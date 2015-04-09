package edu.passwordStorrager.core;

import java.util.Properties;

public interface Application {

    //    SYSTEM
    Properties SYSTEM = System.getProperties();
    String OS_NAME = SYSTEM.getProperty("os.name");
    String OS_VERSION = SYSTEM.getProperty("os.version");
    String OS_ARCH = SYSTEM.getProperty("os.arch");
    String JAVA_VERSION = SYSTEM.getProperty("java.version");

    String USER_HOME = SYSTEM.getProperty("user.home");
    String USER_NAME = SYSTEM.getProperty("user.name");


    boolean IS_MAC = OS_NAME.toLowerCase().contains("mac");
    boolean IS_WINDOWS = OS_NAME.toLowerCase().contains("windows");
    boolean IS_UNIX = OS_NAME.toLowerCase().contains("nix")
            || OS_NAME.toLowerCase().contains("nux")
            || OS_NAME.toLowerCase().contains("aix");

    //    MAC
    String NS_USER_NOTIFICATIONS_BRIDGE_NAME = "NsUserNotificationsBridge.dylib";

    //    APPLICATION
    String APPLICATION_NAME = "Password Storrager";
    String APPLICATION_VERSION = "0.1-beta.6";
    String APPLICATION_FOLDER_NAME = "PasswordStorrager";
    String APPLICATION_TMP_FOLDER = SYSTEM.getProperty("java.io.tmpdir") + Application.APPLICATION_FOLDER_NAME;
    String APPLICATION_LOG_FOLDER_PROPERTY = "PasswordStorrager.log.folder";
    String APPLICATION_LOG_FOLDER = APPLICATION_TMP_FOLDER + "/Logs/";
    String APPLICATION_LIB_FOLDER = APPLICATION_TMP_FOLDER + "/lib/";
    boolean IS_APPLICATION_DEV_MODE = true;
}
