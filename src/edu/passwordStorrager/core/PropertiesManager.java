package edu.passwordStorrager.core;

import edu.passwordStorrager.protector.DefaultValues;

import java.io.*;
import java.util.Properties;

public class PropertiesManager {


    public static final String STORAGE_NAME = "Storage";
    public static final String KEY_NAME = "Key";

    private static final String path = Main.userHome + File.separator + "Library" + File.separator +
            "Application Support" + File.separator + "PasswordStorrager" + File.separator;
    private static final String destination = path + DefaultValues.DEFAULT_SETTINGS_DESTINATION;

    static private void createProperties(String keyPath, String storagePath) {
        File pathSettings = new File(path);
        try {
            pathSettings.mkdir();
            Main.properties.setProperty(STORAGE_NAME, storagePath);
            Main.properties.setProperty(KEY_NAME, keyPath);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            Main.properties.store(byteArrayOutputStream, "");
            byte prop[] = byteArrayOutputStream.toByteArray();
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(prop);
            PasswordProtector.encrypt(byteArrayInputStream, new FileOutputStream(destination));

        } catch (IOException e) {
            System.err.println("Can not create File: " + destination);
        } catch (Throwable throwable) {
            System.err.println("Can not create encrypted File: " + destination);
        }
    }

    public static Properties loadProperties() {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            PasswordProtector.decrypt(new FileInputStream(destination), byteArrayOutputStream);

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
        if (Main.isMac) {
//          createProperties(this.path + EncryptionTable.DEFAULT_STORAGE_DESTINATION);
            return new File(destination).exists();
        } else {
            //TODO make support for win / unix
            return false;
        }
    }

    public static boolean isCorrect() {
        return Main.properties.containsKey("Key");
    }

    public static void showProperties(Properties properties) {
        for (String key : properties.stringPropertyNames()) {
            String value = properties.getProperty(key);
            System.out.println(key + " => " + value);
        }
    }
}
