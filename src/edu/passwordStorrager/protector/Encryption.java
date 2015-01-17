package edu.passwordStorrager.protector;


import edu.passwordStorrager.core.Main;
import edu.passwordStorrager.core.PasswordProtector;
import edu.passwordStorrager.utils.KeyUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

public class Encryption {
    File file;

    public Encryption(File file) {
        this.file = file;
        extractKey();
    }

    public static ByteArrayInputStream decrypt(String filePath) throws Throwable {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PasswordProtector.decrypt(new FileInputStream(filePath), byteArrayOutputStream);
        byte data[] = byteArrayOutputStream.toByteArray();
        return new ByteArrayInputStream(data);
    }

    private void extractKey() {
        if (file.exists()) {
            try {
                Main.key = KeyUtils.loadKeyFile(file.getAbsolutePath());
                System.out.println(Main.key);
            } catch (Throwable throwable) {
                System.err.println("Can not load file: " + file.getAbsolutePath());
            }
        } else {
            //new FirstLaunchDialog();
            System.err.println("Can not load file: " + file.getAbsolutePath());
            //TODO push notification, show dialog message to find / create key
            System.exit(-1);
        }
    }

}
