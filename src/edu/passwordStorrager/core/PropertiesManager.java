package edu.passwordStorrager.core;

import edu.passwordStorrager.protector.Protector;
import edu.passwordStorrager.protector.Values;

import java.io.*;
import java.nio.file.Files;
import java.util.Properties;

public class PropertiesManager {
    public static final String STORAGE_NAME = "Storage";
    public static final String KEY_NAME = "Key";
    
    public static String folder;
    public static String propertiesFilePath;
    public static String framePropertiesFilePath;

    public PropertiesManager() {
        if (Main.IS_MAC) {
            folder = Values.DEFAULT_MAC_PROPERTIES_FILE_FOLDER;
        } else if (Main.IS_WINDOWS) {
            folder = Values.DEFAULT_WINDOWS_PROPERTIES_FILE_FOLDER;
        } else {
            throw new UnsupportedOperationException("This OS is not supported yet");
        }
        propertiesFilePath = folder + Values.DEFAULT_PROPERTIES_FILE_NAME;
        framePropertiesFilePath = folder + Values.DEFAULT_FRAME_PROPERTIES_FILE_NAME;
    }

    private static void createProperties(String keyPath, String storagePath) {
        createFolderDirectory();
        try {
            Main.properties.setProperty(STORAGE_NAME, storagePath);
            Main.properties.setProperty(KEY_NAME, keyPath);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            Main.properties.store(byteArrayOutputStream, "");
            byte prop[] = byteArrayOutputStream.toByteArray();
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(prop);
            Protector.encrypt(byteArrayInputStream, new FileOutputStream(propertiesFilePath));

        } catch (IOException e) {
            System.err.println("Can not create File: " + propertiesFilePath);
        } catch (Throwable throwable) {
            System.err.println("Can not create encrypted File: " + propertiesFilePath);
        }
    }
    
    public static void saveProperties(Properties properties, String filePath) throws IOException {
        properties.store(new FileOutputStream(filePath),"");
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

    public static Properties loadProperties(String propertiesPath) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            Protector.decrypt(new FileInputStream(propertiesPath), byteArrayOutputStream);

            byte data[] = byteArrayOutputStream.toByteArray();

            Properties properties = new Properties();
            properties.load(new ByteArrayInputStream(data));
            return properties;
        } catch (IOException e) {
            System.err.println("Can not load File: " + propertiesPath);
        } catch (Throwable throwable) {
            System.err.println("Can not decrypt File: " + propertiesPath);
        }
        return null;
    }

    public static void changeProperties(String key, String path) {
        createProperties(key, path);

    }



    public static boolean isCorrect() {
        return Main.properties.containsKey(KEY_NAME);
    }

    public static void showProperties(Properties properties) {
        System.out.println("Properties[");
        for (String key : properties.stringPropertyNames()) {
            String value = properties.getProperty(key);
            System.out.println("\t"+key + " => " + value);
        }
        System.out.println("]");
    }
}
