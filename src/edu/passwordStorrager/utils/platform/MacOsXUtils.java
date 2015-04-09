package edu.passwordStorrager.utils.platform;


import com.apple.eawt.*;
import edu.passwordStorrager.core.PasswordStorrager;
import edu.passwordStorrager.gui.*;
import edu.passwordStorrager.xmlManager.XmlParser;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static edu.passwordStorrager.core.Application.IS_MAC;
import static edu.passwordStorrager.core.Core.onQuit;
import static edu.passwordStorrager.core.PasswordStorrager.framesMainForm;
import static edu.passwordStorrager.core.PasswordStorrager.isUnlocked;
import static edu.passwordStorrager.utils.FrameUtils.getCurrentClassName;

public class MacOsXUtils {
    private static final Logger log = Logger.getLogger(getCurrentClassName());
    public static QuitResponse applicationQuitResponse;

    public static void initializeMacOSX() {
        if (IS_MAC) {
            Application application = com.apple.eawt.Application.getApplication();
            //application.requestForeground(false);
            setIcon(application);

            setProperties();

            setHandlers(application);

            addNotificationSupport();

            application.addAppEventListener(new SystemSleepListener() {
                @Override
                public void systemAboutToSleep(AppEvent.SystemSleepEvent systemSleepEvent) {
                    //TODO sync here
                    log.info("System going sleep");
                }

                @Override
                public void systemAwoke(AppEvent.SystemSleepEvent systemSleepEvent) {
                    //TODO sync here
                    log.info("System woke up");
                }
            });

            //application.setDockIconBadge("mac os");

            PopupMenu appDockMenu = new PopupMenu();
            MenuItem lockApp = new MenuItem("Заблокировать");
            lockApp.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    MainForm.blockItem.getActionListeners()[0].actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
                    MainForm.stopLockTimer();
                }
            });
            appDockMenu.add(lockApp);
            application.setDockMenu(appDockMenu);
        }
    }

    private static void addNotificationSupport() {
        if (IS_MAC) {
            File libFolder = new File(edu.passwordStorrager.core.Application.APPLICATION_LIB_FOLDER);
            File NSBridge = new File(edu.passwordStorrager.core.Application.APPLICATION_LIB_FOLDER
                    + edu.passwordStorrager.core.Application.NS_USER_NOTIFICATIONS_BRIDGE_NAME);
            if (!NSBridge.exists()) {
                try {
                    if (!libFolder.exists()) {
                        libFolder.mkdir();
                    } else if (libFolder.isFile()) {
                        libFolder.renameTo(new File(libFolder.getAbsolutePath() + "_"));
                    }

                    InputStream inputStream = MacOsXUtils.class.getResourceAsStream("/resources/NsUserNotificationsBridge.dylib");
                    FileOutputStream fileOutputStream = new FileOutputStream(
                            edu.passwordStorrager.core.Application.APPLICATION_LIB_FOLDER
                                    + edu.passwordStorrager.core.Application.NS_USER_NOTIFICATIONS_BRIDGE_NAME);

                    IOUtils.copy(inputStream, fileOutputStream);
                } catch (IOException e) {
                    log.warn("Can not save NsUserNotificationsBridge.dylib to" +
                            edu.passwordStorrager.core.Application.APPLICATION_LIB_FOLDER, e);
                }
            }
        }
    }

    private static void setHandlers(Application application) {
        application.setAboutHandler(new AboutHandler() {
            @Override
            public void handleAbout(AppEvent.AboutEvent aboutEvent) {
                new AboutApplication();
            }
        });

        application.setPreferencesHandler(new PreferencesHandler() {
            @Override
            public void handlePreferences(AppEvent.PreferencesEvent preferencesEvent) {
                if (PasswordStorrager.isUnlocked) {
                    new SettingsDialog() {
                        @Override
                        public void onOK() {
                            this.saveSettings();
                            if (framesMainForm.size() > 0) {
                                MainForm currentForm = framesMainForm.get(framesMainForm.size() - 1);
                                currentForm.recordArrayList = new XmlParser().parseRecords();
                                currentForm.loadList(currentForm.recordArrayList);
                                currentForm.setEdited(false);
                            }
                            this.dispose();
                        }
                    };
                } else {
                    new ChangeKey() {
                        @Override
                        public void onOK() {
                            saveOptions();
                            dispose();
                        }
                    };
                }
            }
        });

        application.setQuitHandler(new QuitHandler() {
            @Override
            public void handleQuitRequestWith(AppEvent.QuitEvent quitEvent, QuitResponse quitResponse) {
                applicationQuitResponse = quitResponse;
                if (isUnlocked) {
                    onQuit();
                } else {
                    if (!AuthorizeDialog.isBlocked) {
                        onQuit();
                    } else {
                        applicationQuitResponse.cancelQuit();
                    }
                }
            }
        });
        application.setOpenFileHandler(new OpenFilesHandler() {
            @Override
            public void openFiles(AppEvent.OpenFilesEvent openFilesEvent) {
                System.out.println("openfileHandler");
                //?when file tried to open by app? in bundle??
            }
        });

        ScreenSleepListener screenSleepListener = new ScreenSleepListener() {
            @Override
            public void screenAboutToSleep(AppEvent.ScreenSleepEvent screenSleepEvent) {
                System.out.println("screenAboutToSleep");
                MainForm.blockItem.getActionListeners()[0].actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
                MainForm.stopLockTimer();
                //TODO save to tmp file
            }

            @Override
            public void screenAwoke(AppEvent.ScreenSleepEvent screenSleepEvent) {
                System.out.println("screenAwoke");
                //TODO synch here
            }
        };
        application.addAppEventListener(screenSleepListener);

        UserSessionListener userSessionListener = new UserSessionListener() {
            @Override
            public void userSessionDeactivated(AppEvent.UserSessionEvent userSessionEvent) {
                System.out.println("userSessionDeactivated");
                MainForm.blockItem.getActionListeners()[0].actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
                MainForm.stopLockTimer();
                //TODO save to tmp file
            }

            @Override
            public void userSessionActivated(AppEvent.UserSessionEvent userSessionEvent) {
                System.out.println("userSessionActivated");
                //TODO synch here
            }
        };
        application.addAppEventListener(userSessionListener);
    }

    private static void setProperties() {
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", edu.passwordStorrager.core.Application.APPLICATION_NAME);
        System.setProperty("com.apple.laf.AquaLookAndFeel", "true");
        System.setProperty("apple.awt.fileDialogForDirectories", "true");
        System.setProperty("apple.awt.UIElement", "true");
//        System.setProperty("apple.awt.fakefullscreen", "true"); //TODO remove when fullScreen ready //doesn't work?
    }

    private static void setIcon(Application application) {
        if (!isBundled()) {
            application.setDockIconImage(PlatformUtils.appIcon);
        }
    }

    public static boolean isBundled() {
        return PasswordStorrager.JAR_FILE.getAbsolutePath().contains(".app/Contents/Java/") && IS_MAC;
    }
}
