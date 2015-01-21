package edu.passwordStorrager.core;

import com.apple.eawt.Application;
import edu.passwordStorrager.gui.AuthorizeDialog;
import edu.passwordStorrager.gui.FirstLaunchDialog;
import edu.passwordStorrager.gui.MainForm;
import edu.passwordStorrager.objects.Key;
import edu.passwordStorrager.utils.platform.PlatformUtils;
import org.apache.log4j.Logger;

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
    public static boolean isAuthorized = false;
    public static Properties propertiesApplication = new Properties();
    public static Properties propertiesFrames = new Properties();
    public static Application application = com.apple.eawt.Application.getApplication();
    public static ArrayList<MainForm> framesMainForm = new ArrayList<MainForm>();
    public static ArrayList<AuthorizeDialog> framesAuthForm = new ArrayList<AuthorizeDialog>();

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

        PlatformUtils.initializeOS();

        printOSParameters();
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

    
    public static void onQuit() {
        log.debug("Quit");
        //TODO sync here
        System.exit(0);
    }
    
    
    public static void main(String[] args) {
        new Main();
    }
}
