package edu.passwordStorrager.utils.platform;


import com.apple.eawt.*;
import edu.passwordStorrager.core.PasswordStorrager;
import edu.passwordStorrager.gui.*;
import edu.passwordStorrager.xmlManager.XmlParser;
import org.apache.log4j.Logger;

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

            /*PopupMenu p = new PopupMenu("lala");
            p.add(new MenuItem("1"));
            p.add(new MenuItem("2"));
            com.apple.eawt.Application.getApplication().setDockMenu(p);*/
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
    }

    private static void setProperties() {
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", edu.passwordStorrager.core.Application.APPLICATION_NAME);
        System.setProperty("com.apple.laf.AquaLookAndFeel", "true");
        System.setProperty("apple.awt.fileDialogForDirectories", "true");
        System.setProperty("apple.awt.UIElement", "true");
        System.setProperty("apple.awt.fakefullscreen", "true"); //TODO remove when fullScreen ready
    }

    private static void setIcon(Application application) {
        if (!isBundled()) {
            application.setDockIconImage(PlatformUtils.appIcon);
        }
    }

    public static boolean isBundled() {
        return PasswordStorrager.JAR_FILE.getAbsolutePath().contains(".app/") && IS_MAC;
    }
}
