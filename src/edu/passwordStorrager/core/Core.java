package edu.passwordStorrager.core;

import edu.passwordStorrager.gui.AuthorizeDialog;
import edu.passwordStorrager.gui.FirstLaunchDialog;
import edu.passwordStorrager.utils.platform.PlatformUtils;
import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import static edu.passwordStorrager.utils.FileUtils.exists;
import static edu.passwordStorrager.utils.FrameUtils.getCurrentClassName;

public class Core implements Application {
    private static final Logger log = Logger.getLogger(getCurrentClassName());
    
    public Core() {
        log.debug("Launch");

        initSystem();

        if (exists(PropertiesManager.framePropertiesFilePath)) {
            PasswordStorrager.propertiesFrames = new Properties();
            try {
                PasswordStorrager.propertiesFrames.load(new FileInputStream(PropertiesManager.framePropertiesFilePath));
                log.debug("Frames properties loaded from " + PropertiesManager.framePropertiesFilePath);
            } catch (IOException e) {
                log.warn("Can not load Frames properties", e);
            }
        } else {
            PasswordStorrager.propertiesFrames = new Properties();
            log.debug("Creating new Frames properties");
        }

        ////////////////////////////////////////////

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

        System.out.println(PlatformUtils.getSystemParameters());
        PlatformUtils.initializeOS();
    }
    
    public static void onQuit() {
        log.debug("Quit");
        //TODO sync here
        System.exit(0);
    }
}
