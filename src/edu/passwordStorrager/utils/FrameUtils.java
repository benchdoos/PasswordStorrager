package edu.passwordStorrager.utils;

import edu.passwordStorrager.core.Main;
import edu.passwordStorrager.core.PropertiesManager;
import org.apache.log4j.Logger;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.FileInputStream;
import java.io.IOException;

public class FrameUtils {
    private static final Logger log = Logger.getLogger(getCurrentClassName());

    /**
     * Returns the name of called class. Made for usage in static methods.
     * Created to minimize hardcoded code.
     *
     * @return a name of called class.
     */
    public static String getCurrentClassName() {
        try {
            throw new RuntimeException();
        } catch (RuntimeException e) {
            return e.getStackTrace()[1].getClassName();
        }

    }


    public static void setFrameLocation(String className, Point point) {
        Main.frames.setProperty(className + "LX", new Double(point.getX()).intValue() + "");
        Main.frames.setProperty(className + "LY", new Double(point.getY()).intValue() + "");
        try {
            PropertiesManager.saveProperties(Main.frames, PropertiesManager.framePropertiesFilePath);
            Main.frames.load(new FileInputStream(PropertiesManager.framePropertiesFilePath));
        } catch (IOException e) {
            log.warn("Can not write frame location", e);
        }
    }

    public static void setFrameSize(String className, Dimension dimension) {
        Main.frames.setProperty(className + "SW", new Double(dimension.getWidth()).intValue() + "");
        Main.frames.setProperty(className + "SH", new Double(dimension.getHeight()).intValue() + "");
        try {
            PropertiesManager.saveProperties(Main.frames, PropertiesManager.framePropertiesFilePath);

            Main.frames.load(new FileInputStream(PropertiesManager.framePropertiesFilePath));
        } catch (IOException e) {
            log.warn("Can not write frame size", e);
        }
    }

    public static Point getFrameLocation(String className) {
        if (Main.frames != null) {
            try {
                Main.frames.load(new FileInputStream(PropertiesManager.framePropertiesFilePath));

                int x = Integer.parseInt(Main.frames.getProperty(className + "LX"));
                int y = Integer.parseInt(Main.frames.getProperty(className + "LY"));
                return new Point(x, y);
            } catch (Exception e) {
                e.printStackTrace();
                return new Point(50, 50);
            }
        } else {
            return new Point(50, 50);
        }
    }

    public static Dimension getFrameSize(String className) {
        if (Main.frames != null) {
            try {
                Main.frames.load(new FileInputStream(PropertiesManager.framePropertiesFilePath));

                int width = Integer.parseInt(Main.frames.getProperty(className + "SW"));
                int height = Integer.parseInt(Main.frames.getProperty(className + "SH"));
                return new Dimension(width, height);
            } catch (Exception e) {
                return new Dimension(100, 100);
            }

        } else {
            return new Dimension(100, 100);
        }
    }


    public static Point setFrameOnCenter(Dimension size) {
        int width = (int) ((Toolkit.getDefaultToolkit().getScreenSize().width / 2) - (size.getWidth() / 2));
        int height = (int) ((Toolkit.getDefaultToolkit().getScreenSize().height / 2) - (size.getHeight() / 2));
        return new Point(width, height);
    }

    public static void copyToClipboard(String value) {
        StringSelection stringSelection = new StringSelection(value);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }
}