package keyGenerator;

import edu.doos.core.PasswordProtector;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import static edu.doos.core.PasswordProtector.hexPassword;

public class KeyGenerator {
        String hexedDefaultPass;
        String hexedPassword;
    KeyGenerator() {

        hexedDefaultPass = hexPassword("gux882si");
        System.out.println(hexedDefaultPass + "\n");

        hexedPassword = hexPassword(hexedDefaultPass);
        System.out.println(hexedPassword);

        PasswordProtector.PASSWORD = hexedPassword.toCharArray();


        //createDefaultKey();
        xmlConverter();
    }

    void createDefaultKey() {
        //String hexedPassword = hexPassword(EncryptionTable.DEFAULT_PASSWORD_ENCRYPTION);
        /*String hexedDefaultPass = hexPassword("gux882si");
        System.out.println(hexedDefaultPass + "\n");

        String hexedPassword = hexPassword(hexedDefaultPass);
        System.out.println(hexedPassword);

        PasswordProtector.PASSWORD = hexedPassword.toCharArray();*/

        try {
            String encryptedAcc_i = PasswordProtector.encrypt("eugeny.zrazhevsky@gmail.com");
            System.out.println(encryptedAcc_i);
            String encryptedPass_i = PasswordProtector.encrypt("gUx812$i%D*0S_");
            System.out.println(encryptedPass_i);

            String encryptedAcc_m = PasswordProtector.encrypt("eugeny.zrazhevsky@gmail.com");
            System.out.println(encryptedAcc_m);
            String encryptedPass_m = PasswordProtector.encrypt("gUx812$i%doos");
            System.out.println(encryptedPass_m);

            System.out.println();
            System.out.println(PasswordProtector.decrypt(encryptedAcc_i));
            System.out.println(PasswordProtector.decrypt(encryptedPass_i));
            System.out.println(PasswordProtector.decrypt(encryptedAcc_m));
            System.out.println(PasswordProtector.decrypt(encryptedPass_m));
            System.out.println();

            final Properties properties = new Properties();
            properties.setProperty("ENC", hexedPassword);
            properties.setProperty("ACC_iCloud", encryptedAcc_i);
            properties.setProperty("PWD_iCloud", encryptedPass_i);
            properties.setProperty("ACC_MEGA", encryptedAcc_m);
            properties.setProperty("PWD_MEGA", encryptedPass_m);

            for (String key : properties.stringPropertyNames()) {
                String value = properties.getProperty(key);
                System.out.println(key + " => " + value);
            }

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            properties.store(byteArrayOutputStream, "DEFAULT KEY");
            byte prop[] = byteArrayOutputStream.toByteArray();
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(prop);

            PasswordProtector.encrypt(byteArrayInputStream, new FileOutputStream(".key"));
            PasswordProtector.decrypt(new FileInputStream(".key"), new FileOutputStream("key.txt"));

        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
    void xmlConverter() {
        try {
            PasswordProtector.encrypt(new FileInputStream("storage.xml"), new FileOutputStream("encrypted"));
            PasswordProtector.decrypt(new FileInputStream("encrypted"), new FileOutputStream("decrypted.xml"));
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new KeyGenerator();
    }
}
