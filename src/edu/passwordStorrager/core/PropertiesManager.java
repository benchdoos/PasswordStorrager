package edu.passwordStorrager.core;

import edu.passwordStorrager.protector.Protector;
import edu.passwordStorrager.protector.Values;

import java.io.*;
import java.nio.file.Files;
import java.util.Properties;

public class PropertiesManager {
    public static final String STORAGE_NAME = "Storage";
    public static final String KEY_NAME = "Key";
    
    private static String folder;
    private static String destination;

    public PropertiesManager() {
        if (Main.isMac) {
            folder = Values.DEFAULT_MAC_PROPERTIES_FILE_FOLDER;
        } else if (Main.isWindows) {
            folder = Values.DEFAULT_WINDOWS_PROPERTIES_FILE_FOLDER;
        } else {
            throw new UnsupportedOperationException("This OS is not supported yet");
        }
        destination = folder + Values.DEFAULT_PROPERTIES_FILE_NAME;
    }

    static private void createProperties(String keyPath, String storagePath) {
        createFolderDirectory();
        try {
            Main.properties.setProperty(STORAGE_NAME, storagePath);
            Main.properties.setProperty(KEY_NAME, keyPath);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            Main.properties.store(byteArrayOutputStream, "");
            byte prop[] = byteArrayOutputStream.toByteArray();
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(prop);
            Protector.encrypt(byteArrayInputStream, new FileOutputStream(destination));

        } catch (IOException e) {
            System.err.println("Can not create File: " + destination);
        } catch (Throwable throwable) {
            System.err.println("Can not create encrypted File: " + destination);
        }
    }

    private static void createFolderDirectory() {
        File folder_ = new File(folder);
        if(folder_.exists()) {
            if (!folder_.isDirectory()) {
                try {
                    Files.copy(folder_.toPath(), new FileOutputStream(folder + "_bak"));
                    folder_.delete();
                } catch (IOException e) {
                    System.err.println("can not copy file");
                }
            }
        } else {
            folder_.mkdir();
        }
    }

    public static Properties loadProperties() {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            Protector.decrypt(new FileInputStream(destination), byteArrayOutputStream);

            byte data[] = byteArrayOutputStream.toByteArray();

            Properties properties = new Properties();
            properties.load(new ByteArrayInputStream(data));
            return properties;
        } catch (IOException e) {
            System.err.println("Can not load File: " + destination);
        } catch (Throwable throwable) {
            System.err.println("Can not decrypt File: " + destination);
        }
        return null;
    }

    public static void changeProperties(String key, String path) {
        createProperties(key, path);

    }

    public static boolean exists() {
        return (Main.isMac || Main.isWindows) && new File(destination).exists();
    }

    public static boolean isCorrect() {
        return Main.properties.containsKey(KEY_NAME);
    }

    public static void showProperties(Properties properties) {
        System.out.println("Properties[");
        for (String key : properties.stringPropertyNames()) {
            String value = properties.getProperty(key);
            System.out.println(key + " => " + value);
        }
        System.out.println("]");
    }
}
