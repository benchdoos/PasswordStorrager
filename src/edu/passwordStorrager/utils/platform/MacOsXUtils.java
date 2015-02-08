package edu.passwordStorrager.utils.platform;

import com.apple.eawt.*;
import edu.passwordStorrager.core.Main;
import edu.passwordStorrager.core.PropertiesManager;
import edu.passwordStorrager.gui.AboutApplication;
import edu.passwordStorrager.gui.ChangeKey;
import edu.passwordStorrager.gui.MainForm;
import edu.passwordStorrager.gui.SettingsDialog;
import edu.passwordStorrager.protector.Values;
import edu.passwordStorrager.xmlManager.XmlParser;
import org.apache.log4j.Logger;

import java.io.File;

import static edu.passwordStorrager.core.Core.onQuit;
import static edu.passwordStorrager.core.Main.*;
import static edu.passwordStorrager.utils.FrameUtils.getCurrentClassName;

public class MacOsXUtils {
    private static final Logger log = Logger.getLogger(getCurrentClassName());
    
    public static void initializeMacOSX() {
        if (IS_MAC) {
            Application application = com.apple.eawt.Application.getApplication();
            //application.requestForeground(false);
            application.setDockIconImage(PlatformUtils.icon);

            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "PasswordStorrager");
            System.setProperty("com.apple.laf.AquaLookAndFeel", "true");
            System.setProperty("apple.awt.fileDialogForDirectories", "true");
            System.setProperty("apple.awt.UIElement", "true");
            System.setProperty("apple.awt.fakefullscreen", "true"); //TODO remove when fullScreen ready

            application.setAboutHandler(new AboutHandler() {
                @Override
                public void handleAbout(AppEvent.AboutEvent aboutEvent) {
                    new AboutApplication();
                }
            });

            application.setPreferencesHandler(new PreferencesHandler() {
                @Override
                public void handlePreferences(AppEvent.PreferencesEvent preferencesEvent) {
                    if (isAuthorized) {
                        new SettingsDialog() {
                            @Override
                            public void onOK() {
                                this.saveSettings();
                                if (framesMainForm.size() > 0) {
                                    MainForm currentForm = framesMainForm.get(framesMainForm.size() - 1);
                                    currentForm.recordArrayList = new XmlParser().parseRecords();
                                    currentForm.loadList(currentForm.recordArrayList);
                                    currentForm.updateTitle(new File(Main.propertiesApplication.getProperty(PropertiesManager.KEY_NAME) + Values.DEFAULT_STORAGE_FILE_NAME));
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
                    onQuit();
                }
            });

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

            application.setOpenFileHandler(new OpenFilesHandler() {
                @Override
                public void openFiles(AppEvent.OpenFilesEvent openFilesEvent) {
                    System.out.println("openfileHandler");
                    //?when file tried to open by app? in bundle??
                }
            });

            //application.setDockIconBadge("mac os");

            /*PopupMenu p = new PopupMenu("lala");
            p.add(new MenuItem("1"));
            p.add(new MenuItem("2"));
            com.apple.eawt.Application.getApplication().setDockMenu(p);*/
        }
    }
}
