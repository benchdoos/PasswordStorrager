package edu.passwordStorrager.utils;

import edu.passwordStorrager.core.Main;
import edu.passwordStorrager.core.PropertiesManager;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

    public static String getCurrentClassName(Class clazz) {
        return clazz.getCanonicalName();
    }


    public static void setFrameLocation(String className, Point point) {
        Main.propertiesFrames.setProperty(className + "LX", new Double(point.getX()).intValue() + "");
        Main.propertiesFrames.setProperty(className + "LY", new Double(point.getY()).intValue() + "");
        try {
            PropertiesManager.saveProperties(Main.propertiesFrames, PropertiesManager.framePropertiesFilePath);
            Main.propertiesFrames.load(new FileInputStream(PropertiesManager.framePropertiesFilePath));
        } catch (IOException e) {
            log.warn("Can not write frame location", e);
        }
    }

    public static void setFrameSize(String className, Dimension dimension) {
        Main.propertiesFrames.setProperty(className + "SW", new Double(dimension.getWidth()).intValue() + "");
        Main.propertiesFrames.setProperty(className + "SH", new Double(dimension.getHeight()).intValue() + "");
        try {
            PropertiesManager.saveProperties(Main.propertiesFrames, PropertiesManager.framePropertiesFilePath);

            Main.propertiesFrames.load(new FileInputStream(PropertiesManager.framePropertiesFilePath));
        } catch (IOException e) {
            log.warn("Can not write frame size", e);
        }
    }

    public static Point getFrameLocation(String className) {
        if (Main.propertiesFrames != null) {
            try {
                Main.propertiesFrames.load(new FileInputStream(PropertiesManager.framePropertiesFilePath));

                int x = Integer.parseInt(Main.propertiesFrames.getProperty(className + "LX"));
                int y = Integer.parseInt(Main.propertiesFrames.getProperty(className + "LY"));
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
        if (Main.propertiesFrames != null) {
            try {
                Main.propertiesFrames.load(new FileInputStream(PropertiesManager.framePropertiesFilePath));

                int width = Integer.parseInt(Main.propertiesFrames.getProperty(className + "SW"));
                int height = Integer.parseInt(Main.propertiesFrames.getProperty(className + "SH"));
                return new Dimension(width, height);
            } catch (Exception e) {
                return new Dimension(100, 100);
            }

        } else {
            return new Dimension(100, 100);
        }
    }

    public static void shakeFrame(final Component component) {
        final Timer timer = new Timer(60, null);
        timer.addActionListener(new ActionListener() {
            int counter = 0;
            final int maxCounter = 6;
            int step = 10;
            final Point location = component.getLocation();

            @Override
            public void actionPerformed(ActionEvent e) {
                if (counter <= 2) {
                    step = 10;
                } else if (counter <= 4) {
                    step = 5;
                } else if (counter <= 6) {
                    step = 3;
                }
                if (counter <= maxCounter) {
                    if (counter % 2 == 0) {
                        component.setLocation(location.x + step, location.y);
                    } else {
                        component.setLocation(location.x - step, location.y);
                    }
                } else {
                    component.setLocation(location.x, location.y);
                    timer.stop();
                }
                counter++;
            }
        });
        timer.start();
    }

    public static Window getWindow(String frameClassName) {
        if (Main.frames != null) {
            for (int i = 0; i < Main.frames.size(); i++) {
                Window window = Main.frames.get(i);
                System.out.println(i + "current:" + getCurrentClassName(window.getClass()) +
                        "\ninc:" + frameClassName);
                if (frameClassName != null && getCurrentClassName(window.getClass()) != null) {
                    System.out.println("[][]3[" + getCurrentClassName(window.getClass()));
                    System.out.println("[][]4[" + frameClassName);
                    if (getCurrentClassName(window.getClass()).equals(frameClassName)) {
                        System.out.println("returning window");
                        return window;
                    }
                }

            }
        }
        return null;
    }

    public static Point setFrameOnCenter(Dimension size) {
        int width = (int) ((Toolkit.getDefaultToolkit().getScreenSize().width / 2) - (size.getWidth() / 2));
        int height = (int) ((Toolkit.getDefaultToolkit().getScreenSize().height / 2) - (size.getHeight() / 2));
        return new Point(width, height);
    }

    public static void copyToClipboard(String value) {
        if (!value.isEmpty()) {
            StringSelection stringSelection = new StringSelection(value);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
        }
    }
}
