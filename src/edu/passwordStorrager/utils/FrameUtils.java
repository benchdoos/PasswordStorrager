package edu.passwordStorrager.utils;

import edu.passwordStorrager.core.Application;
import edu.passwordStorrager.core.PropertiesManager;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import static edu.passwordStorrager.core.PasswordStorrager.frames;
import static edu.passwordStorrager.core.PasswordStorrager.propertiesFrames;

public class FrameUtils {
    private static final Logger log = Logger.getLogger(getCurrentClassName());
    private static final Timer timer = new Timer(60, null);

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
        propertiesFrames.setProperty(className + "LX", new Double(point.getX()).intValue() + "");
        propertiesFrames.setProperty(className + "LY", new Double(point.getY()).intValue() + "");
        try {
            PropertiesManager.saveProperties(propertiesFrames, PropertiesManager.framePropertiesFilePath);
            propertiesFrames.load(new FileInputStream(PropertiesManager.framePropertiesFilePath));
        } catch (IOException e) {
            log.warn("Can not write frame location", e);
        }
    }

    public static void setFrameSize(String className, Dimension dimension) {
        propertiesFrames.setProperty(className + "SW", new Double(dimension.getWidth()).intValue() + "");
        propertiesFrames.setProperty(className + "SH", new Double(dimension.getHeight()).intValue() + "");
        try {
            PropertiesManager.saveProperties(propertiesFrames, PropertiesManager.framePropertiesFilePath);

            propertiesFrames.load(new FileInputStream(PropertiesManager.framePropertiesFilePath));
        } catch (IOException e) {
            log.warn("Can not write frame size", e);
        }
    }

    public static Point getFrameLocation(String className) {
        if (propertiesFrames != null) {
            try {
                propertiesFrames.load(new FileInputStream(PropertiesManager.framePropertiesFilePath));

                int x = Integer.parseInt(propertiesFrames.getProperty(className + "LX"));
                int y = Integer.parseInt(propertiesFrames.getProperty(className + "LY"));
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
        if (propertiesFrames != null) {
            try {
                propertiesFrames.load(new FileInputStream(PropertiesManager.framePropertiesFilePath));

                int width = Integer.parseInt(propertiesFrames.getProperty(className + "SW"));
                int height = Integer.parseInt(propertiesFrames.getProperty(className + "SH"));
                return new Dimension(width, height);
            } catch (Exception e) {
                return new Dimension(100, 100);
            }

        } else {
            return new Dimension(100, 100);
        }
    }


    public static void shakeFrame(final Component component) {
        final Window window = findWindow(component);

        if (!timer.isRunning()) {
            timer.addActionListener(new ActionListener() {
                final Point location = window.getLocation();
                int counter = 0;
                final int maxCounter = 6;
                int step = 14;

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (counter <= 2) {
                        step = 14;
                    } else if (counter > 2 && counter <= 4) {
                        step = 7;
                    } else if (counter > 4 && counter <= 6) {
                        step = 3;
                    }

                    if (counter <= maxCounter) {
                        counter++;
                        if (counter % 2 == 1) {
                            Point newLocation = new Point(location.x + step, location.y);
                            window.setLocation(newLocation);
                        } else {
                            Point newLocation = new Point(location.x - step, location.y);
                            window.setLocation(newLocation);
                        }
                    } else {
                        Point newLocation = new Point(location.x, location.y);
                        window.setLocation(newLocation);

                        counter = 0;
                        timer.removeActionListener(timer.getActionListeners()[0]);
                        timer.stop();
                    }
                }
            });
            timer.start();
        }
        Toolkit.getDefaultToolkit().beep();
    }


    public static Window findWindow(Component c) {
        if (c == null) {
            return JOptionPane.getRootFrame();
        } else if (c instanceof Window) {
            return (Window) c;
        } else {
            return findWindow(c.getParent());
        }
    }


    public static ArrayList<Window> getWindows(Class clazz) {

        if (frames != null) {
            ArrayList<Window> windows = new ArrayList<>();
            for (Window window : frames) {
                if (getCurrentClassName(window.getClass()).equals(getCurrentClassName(clazz))) {
                    windows.add(window);
                }
            }

            return windows;
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

    public static JFileChooser getFolderChooser(String title) {
        UIManager.put("FileChooser.updateButtonText", "true");
        UIManager.put("FileChooser.filesOfTypeLabelText", "Выбрать:");
        UIManager.put("FileChooser.newFolderToolTipText", "Новая папка");
        UIManager.put("FileChooser.cancelButtonText", "Отмена");

        JFileChooser fileChooser = new JFileChooser(Application.USER_HOME);
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setControlButtonsAreShown(true);
        fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
        fileChooser.setDialogTitle(title);
        fileChooser.setApproveButtonText("Выбрать");
        fileChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory();
            }

            @Override
            public String getDescription() {
                return "Папки";
            }
        });

        ArrayList<JPanel> panels = new ArrayList<>();
        for (Component c : fileChooser.getComponents()) {
            if (c instanceof JPanel) {
                panels.add((JPanel) c);
            }
        }
        panels.get(0).getComponent(0).setVisible(false);
        return fileChooser;
    }

    public static Icon resizeIcon(URL url, Dimension size) {
        try {
            BufferedImage img = ImageIO.read(url);
            return resizeIcon(img, size);
        } catch (IOException e) {
            log.warn("Can not load file: /resources/icons/icon_black_256.png");
            return null;
        }
    }

    public static Icon resizeIcon(Image image, Dimension size) {
        Image scaledImage = image.getScaledInstance(size.width, size.height,
                Image.SCALE_SMOOTH);
        return new ImageIcon(scaledImage);
    }

    public static void registerWindow(Window w) {
        frames.add(w);
    }

    public static void removeWindow(Window w) {
        Window d=null;
        for (Window win : frames) {
            if (win.equals(w)) {
                System.out.println("~~~");
                d = win;
                break;
            }
        }
        if (d != null) {
            frames.remove(d);
        }
    }
}
