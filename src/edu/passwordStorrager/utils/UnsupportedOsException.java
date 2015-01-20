package edu.passwordStorrager.utils;


import edu.passwordStorrager.core.Main;
import edu.passwordStorrager.gui.NotificationDialog;
import org.apache.log4j.Logger;

import static edu.passwordStorrager.utils.FrameUtils.getCurrentClassName;

public class UnsupportedOsException extends RuntimeException {
    private static final Logger log = Logger.getLogger(getCurrentClassName());

    public UnsupportedOsException() {
        log.fatal("This OS is not supported yet : " + Main.OS_NAME);
        String title = Main.OS_NAME + " не поддерживается";
        String message = "<html>Операционная система <b>" + Main.OS_NAME + "</b> не поддерживается на данный момент.</html>";
            new NotificationDialog(title, message, NotificationDialog.NOTIFICATION_ERROR) {
                @Override
                public void onOK() {
                    Main.onExit();
                    dispose();
                }
            };
        System.exit(-1);
    }
}
