package edu.passwordStorrager.protector;


import edu.passwordStorrager.core.PasswordStorrager;
import edu.passwordStorrager.gui.FirstLaunchDialog;
import edu.passwordStorrager.gui.NotificationDialog;
import edu.passwordStorrager.utils.KeyUtils;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

import static edu.passwordStorrager.utils.FrameUtils.getCurrentClassName;

public class Encryption {
    private static final Logger log = Logger.getLogger(getCurrentClassName());

    File file;

    public static ByteArrayInputStream decrypt(String filePath) throws Throwable {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Protector.decrypt(new FileInputStream(filePath), byteArrayOutputStream);
        byte data[] = byteArrayOutputStream.toByteArray();
        return new ByteArrayInputStream(data);
    }

    public static void extractKey(File file) {
        if (file.exists()) {
            try {
                PasswordStorrager.key = KeyUtils.loadKeyFile(file.getAbsolutePath());
            } catch (Throwable e) {
                log.warn("Can not load file: " + file.getAbsolutePath(), e);
            }
        } else {
            //new FirstLaunchDialog();
            log.fatal("Can not load file: " + file.getAbsolutePath());
            //TODO push notification, show dialog message to find / create key
            new NotificationDialog("Ошибка", "<html><body>Не удалось найти файл :<br>"
                    + file.getAbsolutePath() + "</body></html>",
                    NotificationDialog.NOTIFICATION_ERROR){
                @Override
                public void onOK() {
                    new FirstLaunchDialog();
                    dispose();
                }
            };
        }
    }

}
