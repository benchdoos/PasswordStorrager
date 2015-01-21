package edu.passwordStorrager.core;

import com.apple.eawt.*;
import edu.passwordStorrager.gui.*;
import edu.passwordStorrager.objects.Key;
import edu.passwordStorrager.protector.Values;
import edu.passwordStorrager.utils.UnsupportedOsException;
import edu.passwordStorrager.xmlManager.XmlParser;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import static edu.passwordStorrager.utils.FileUtils.exists;
import static edu.passwordStorrager.utils.FrameUtils.getCurrentClassName;

public class Main {
    private static final Logger log = Logger.getLogger(getCurrentClassName());

    public static final String version = "0.1.b.9";

    public static final Properties system = System.getProperties();
    public static final String OS_NAME = system.getProperty("os.name");
    public static final String USER_HOME = system.getProperty("user.home");
    public static final boolean IS_MAC = OS_NAME.toLowerCase().equals("mac os x");
    public static final boolean IS_WINDOWS = OS_NAME.toLowerCase().equals("windows");


    public static Key key = new Key();
    public static Properties propertiesApplication = new Properties();
    public static Properties propertiesFrames = new Properties();
    public static Application application = com.apple.eawt.Application.getApplication();
    public static ArrayList<MainForm> framesMainForm = new ArrayList<MainForm>();

    private Image icon = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/resources/icons/icon_black.png"));

    Main() {
        log.debug("Launch");

        initSystem();

        if (exists(PropertiesManager.framePropertiesFilePath)) {
            Main.propertiesFrames = new Properties();
            try {
                Main.propertiesFrames.load(new FileInputStream(PropertiesManager.framePropertiesFilePath));
                log.debug("Frames properties loaded from " + PropertiesManager.framePropertiesFilePath);
            } catch (IOException e) {
                log.warn("Can not load Frames properties", e);
            }
        } else {
            Main.propertiesFrames = new Properties();
            log.debug("Creating new Frames properties");
        }

        if (exists(PropertiesManager.propertiesFilePath)) {
            log.debug("Properties file found, loading AuthorizeDialog");
            new AuthorizeDialog();
        } else {
            log.debug("Properties file not found, loading FirstLaunchDialog");
            new FirstLaunchDialog();
        }
    }

    private void initSystem() {
        new PropertiesManager(); //MUST BE CALLED. DO NOT TOUCH

        initializeOS();

        printOSParameters();
    }

    public static boolean isOsSupported() {
        return IS_MAC;
    }

    private void printOSParameters() {
        System.out.println("==========================System=========================");
        System.out.println("System:");
        System.out.println("\tOS: " + OS_NAME + " v." + system.getProperty("os.version") + " arch: " + system.getProperty("os.arch"));
        System.out.println("Java:");
        System.out.println("\tJava version: " + system.getProperty("java.specification.version") + "(" + system.getProperty("java.version") + ")");
        System.out.println("\t" + system.getProperty("java.runtime.name") + " v." + system.getProperty("java.vm.version"));
        System.out.println("User:");
        System.out.println("\tName: " + system.getProperty("user.name") + " Home: " + USER_HOME);
        System.out.println("\tTime zone: " + system.getProperty("user.timezone") + " (" + system.getProperty("user.country") + ") language: " + system.getProperty("user.language"));
        System.out.println("Logging to: " + system.getProperty("java.io.tmpdir") + "PasswordStorrager/Logs/");
        System.out.println("=========================================================");
    }


    private void initializeOS() {
        log.info("System - OS: " + OS_NAME + " v." + system.getProperty("os.version") + " " + system.getProperty("os.arch") + "; Java v." + system.getProperty("java.version"));
        if (!isOsSupported()) {
            throw new UnsupportedOsException();
        }

        if (IS_MAC) {
            initializeMacOSX();
        } else if (IS_WINDOWS) {
            //osHandler here
        }
    }

    private void initializeMacOSX() {
        if (IS_MAC) {
            application.requestForeground(false);
            application.setDockIconImage(icon);

            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "PasswordStorrager");

            System.setProperty("com.apple.laf.AquaLookAndFeel", "true");
            System.setProperty("apple.awt.fileDialogForDirectories", "true");

            application.setAboutHandler(new AboutHandler() {
                @Override
                public void handleAbout(AppEvent.AboutEvent aboutEvent) {
                    new AboutApplication();
                }
            });

            application.setPreferencesHandler(new PreferencesHandler() {
                @Override
                public void handlePreferences(AppEvent.PreferencesEvent preferencesEvent) {
                    new SettingsDialog() {
                        @Override
                        public void onOK() {
                            this.saveSettings();
                            if(framesMainForm.size()>0) {
                                MainForm currentForm = framesMainForm.get(framesMainForm.size() - 1);
                                currentForm.recordArrayList = new XmlParser().parseRecords();
                                currentForm.loadList();
                                currentForm.getRootPane().putClientProperty("Window.documentFile", new File(Main.propertiesApplication.getProperty(PropertiesManager.KEY_NAME) + Values.DEFAULT_STORAGE_FILE_NAME));
                                currentForm.setEdited(false);
                            }
                            this.dispose();
                        }
                    };
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
                    System.out.println("sync here");
                }

                @Override
                public void systemAwoke(AppEvent.SystemSleepEvent systemSleepEvent) {
                    System.out.println("sync here");
                }
            });

            //application.setDockIconBadge("mac os");

            /*PopupMenu p = new PopupMenu("lala");
            p.add(new MenuItem("1"));
            p.add(new MenuItem("2"));
            com.apple.eawt.Application.getApplication().setDockMenu(p);*/
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | InstantiationException | UnsupportedLookAndFeelException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public static void initMacApplication() {
       /* application.addApplicationListener(
                new ApplicationAdapter() {
                    public void handleAbout(ApplicationEvent applicationEvent) {
                        System.out.println("about");
                        applicationEvent.setHandled(true);
                    }

                    public void handlePreferences(ApplicationEvent applicationEvent) {
                        new SettingsDialog() {
                            @Override
                            public void onOK() {

                            }
                        };
                    }

                    public void handleQuit(ApplicationEvent applicationEvent) {
                        System.out.println("quit");
                    }

                    public void handleOpenFile(ApplicationEvent applicationEvent) {
                                                   *//*Project project = getProject();
                                                   String filename = applicationEvent.getFilename();
                                                   if (filename == null) return;
                                                   File file = new File(filename);
                                                   if (ProjectUtil.openOrImport(file.getAbsolutePath(), project, true) != null) {
                                                       IdeaApplication.getInstance().setPerformProjectLoad(false);
                                                       return;
                                                   }
                                                   if (project != null && file.exists()) {
                                                       OpenFileAction.openFile(filename, project);
                                                       applicationEvent.setHandled(true);
                                                   }*//*
                        System.out.println("open file");
                    }
                }
        );*/
//        application.addAboutMenuItem();
//        application.addPreferencesMenuItem();
//        application.setEnabledAboutMenu(true);
//        application.setEnabledPreferencesMenu(false);
        //installAutoUpdateMenu();
    }


    public static void onQuit() {
        log.debug("Quit");
        //TODO sync here
        System.exit(0);
    }

    public static void onForceQuit() {
        log.warn("Application forced quit");
    }

    public static void main(String[] args) {
        new Main();
    }
}
