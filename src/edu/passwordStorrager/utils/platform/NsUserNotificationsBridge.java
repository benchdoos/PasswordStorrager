package edu.passwordStorrager.utils.platform;

import com.sun.jna.Library;
import com.sun.jna.Native;
import edu.passwordStorrager.core.Application;

public interface NsUserNotificationsBridge extends Library {


    NsUserNotificationsBridge instance = (NsUserNotificationsBridge)
            Native.loadLibrary(Application.APPLICATION_LIB_FOLDER
                            + Application.NS_USER_NOTIFICATIONS_BRIDGE_NAME,
                    NsUserNotificationsBridge.class);

    int sendNotification(String title, String subtitle, String text, int timeOffset);


}


