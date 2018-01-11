package edu.passwordStorrager.utils.platform;


import edu.passwordStorrager.core.Application;
import edu.passwordStorrager.core.Core;
import edu.passwordStorrager.gui.NotificationDialog;
import org.apache.log4j.Logger;

import static edu.passwordStorrager.utils.FrameUtils.getCurrentClassName;

public class UnsupportedOsException extends RuntimeException {
    private static final Logger log = Logger.getLogger(getCurrentClassName());

    public UnsupportedOsException() {
        log.fatal("This OS is not supported yet : " + Application.OS_NAME);
        String title = Application.OS_NAME + " не поддерживается";
        String message = "<html>Операционная система <b>" + Application.OS_NAME + "</b> не поддерживается на данный момент.</html>";
            new NotificationDialog(title, message, NotificationDialog.NOTIFICATION_ERROR) {
                @Override
                public void onOK() {
                    Core.onQuit();
                    dispose();
                }
            };
        //Core.onQuit();
    }
}
