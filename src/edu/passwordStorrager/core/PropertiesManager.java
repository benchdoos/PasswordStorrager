package edu.passwordStorrager.core;

import edu.passwordStorrager.protector.Protector;
import edu.passwordStorrager.protector.Values;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.util.Properties;

import static edu.passwordStorrager.utils.FrameUtils.getCurrentClassName;

public class PropertiesManager {
    private static final Logger log = Logger.getLogger(getCurrentClassName());

    public static final String STORAGE_NAME = "Storage";
    public static final String KEY_NAME = "Key";
    public static final String LOCK_DELAY = "LockDelay";

    public static String folder;
    public static String propertiesFilePath;
    public static String framePropertiesFilePath;
    private static int delay = Values.DEFAULT_LOCK_DELAY;

    public PropertiesManager() {
        if (Application.IS_MAC) {
            folder = Values.DEFAULT_MAC_PROPERTIES_FILE_FOLDER;
        } else if (Application.IS_WINDOWS) {
            folder = Values.DEFAULT_WINDOWS_PROPERTIES_FILE_FOLDER;
        } else if (Application.IS_UNIX) {
            folder = Values.DEFAULT_UNIX_PROPERTIES_FILE_FOLDER;
        } else {
            throw new UnsupportedOperationException("This OS is not supported yet");
        }


        propertiesFilePath = folder + Values.DEFAULT_PROPERTIES_FILE_NAME;
        framePropertiesFilePath = folder + Values.DEFAULT_FRAME_PROPERTIES_FILE_NAME;
        System.setProperty("PropertiesFolder", folder);
    }

    private static void createProperties(String keyPath, String storagePath) {
        createFolderDirectory();
        try {
            PasswordStorrager.propertiesApplication.setProperty(STORAGE_NAME, storagePath);
            PasswordStorrager.propertiesApplication.setProperty(KEY_NAME, keyPath);
            PasswordStorrager.propertiesApplication.setProperty(LOCK_DELAY, delay + "");

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            PasswordStorrager.propertiesApplication.store(byteArrayOutputStream, "");
            byte prop[] = byteArrayOutputStream.toByteArray();
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(prop);
            Protector.encrypt(byteArrayInputStream, new FileOutputStream(propertiesFilePath));

        } catch (IOException e) {
            log.warn("Can not create File: " + propertiesFilePath);
        } catch (Throwable throwable) {
            log.warn("Can not create encrypted File: " + propertiesFilePath);
        }
    }

    public static void saveProperties(Properties properties, String filePath) throws IOException {
        properties.store(new FileOutputStream(filePath), "");
    }

    private static void createFolderDirectory() {
        File folder_ = new File(folder);
        if (folder_.exists()) {
            if (!folder_.isDirectory()) {
                try {
                    Files.copy(folder_.toPath(), new FileOutputStream(folder + "_bak"));
                    folder_.delete();
                } catch (IOException e) {
                    log.warn("can not copy file");
                }
            }
        } else {
            folder_.mkdir();
        }
    }

    public static Properties loadProperties(String propertiesPath) {
        if (Application.IS_APPLICATION_DEV_MODE) {
            String dev = PasswordStorrager.JAR_FILE + "/edu/passwordStorrager/core/storage.properties";
            if (new File(dev).exists()) {
                System.err.println("DEV_MODE:" + dev);
                //propertiesPath = dev; //TODO fix on windows
            }
        }
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            Protector.decrypt(new FileInputStream(propertiesPath), byteArrayOutputStream);

            byte data[] = byteArrayOutputStream.toByteArray();

            Properties properties = new Properties();
            properties.load(new ByteArrayInputStream(data));
            return properties;
        } catch (IOException e) {
            log.warn("Can not load File: " + propertiesPath);
        } catch (Throwable throwable) {
            log.warn("Can not decrypt File: " + propertiesPath);
        }
        return null;
    }

    public static void changeProperties(String key, String path) {
        createProperties(key, path);

    }


    public static void changeLockDelay(int delay) {
        PropertiesManager.delay = delay;
        changeProperties(PasswordStorrager.propertiesApplication.getProperty(STORAGE_NAME),
                PasswordStorrager.propertiesApplication.getProperty(KEY_NAME));
    }

    public static boolean isCorrect() {
        return PasswordStorrager.propertiesApplication.containsKey(KEY_NAME);
    }

    public static void showProperties(Properties properties) {
        System.out.println("Properties[");
        for (String key : properties.stringPropertyNames()) {
            String value = properties.getProperty(key);
            System.out.println("\t" + key + " => " + value);
        }
        System.out.println("]");
    }
}
