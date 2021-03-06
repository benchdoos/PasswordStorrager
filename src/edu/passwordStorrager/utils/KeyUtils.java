package edu.passwordStorrager.utils;

import edu.passwordStorrager.objects.Key;
import edu.passwordStorrager.protector.Encryption;
import edu.passwordStorrager.protector.Protector;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;

public class KeyUtils {

    public static void createKeyFile(Key key, String filePath) throws Throwable {
        Properties properties = new Properties();
        if (!key.getENC().isEmpty()) {
            properties.setProperty(Key.ENC_Value, key.getENC());
            properties = loadICloudParams(properties, key);
            properties = loadMegaParams(properties, key);
            properties = loadDropBoxParams(properties, key);
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        properties.store(byteArrayOutputStream, "CUSTOM KEY");
        byte prop[] = byteArrayOutputStream.toByteArray();
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(prop);
        Protector.encrypt(byteArrayInputStream,
                new FileOutputStream(filePath));
    }

    public static Key loadKeyFile(String filePath) throws Throwable {
        Key key = new Key();
        boolean isCorrect= false;
        try {
            isCorrect = isKeyCorrect(filePath);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        if(isCorrect) {
            System.out.println("Loading custom key file : " + filePath);
            Properties properties = new Properties();

            properties.load(Encryption.decrypt(filePath));

            key.setENC(properties.getProperty(Key.ENC_Value));
            key.setICloud(properties.getProperty(Key.iCloudAcc), properties.getProperty(Key.iCloudPwd));
            key.setMega(properties.getProperty(Key.megaAcc), properties.getProperty(Key.megaPwd));
            key.setDropBox(properties.getProperty(Key.dropBoxAcc), properties.getProperty(Key.dropBoxPwd));

            System.out.println(key);
            return key;
        } else {
            throw new java.security.InvalidKeyException("Key is not correct / can not be decoded");
        }
    }

    private static boolean isKeyCorrect(String filePath) throws Throwable {
        if(new File(filePath).exists()) {
            Properties properties = new Properties();
            properties.load(Encryption.decrypt(filePath));
            return properties.getProperty(Key.ENC_Value) != null;
        } else {
            return false;
        }
        
    }

    private static Properties loadICloudParams(Properties properties, Key key) {
        if (key != null) {
            if (!key.getICloudLogin().isEmpty() && !key.getICloudPassword().isEmpty()) {
                properties.setProperty(Key.iCloudAcc, key.getICloudLogin());
                properties.setProperty(Key.iCloudPwd, key.getICloudPassword());
            }
        }
        return properties;
    }

    private static Properties loadMegaParams(Properties properties, Key key) {
        if (!key.getMegaLogin().isEmpty() && !key.getMegaPassword().isEmpty()) {
            properties.setProperty(Key.megaAcc, key.getMegaLogin());
            properties.setProperty(Key.megaPwd, key.getMegaPassword());
        }
        return properties;
    }

    private static Properties loadDropBoxParams(Properties properties, Key key) {
        if (!key.getDropBoxLogin().isEmpty() && !key.getICloudPassword().isEmpty()) {
            properties.setProperty(Key.dropBoxAcc, key.getDropBoxLogin());
            properties.setProperty(Key.dropBoxPwd, key.getDropBoxLogin());
        }
        return properties;
    }
}
