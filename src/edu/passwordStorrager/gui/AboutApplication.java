package edu.passwordStorrager.gui;

import edu.passwordStorrager.core.Application;
import edu.passwordStorrager.utils.FrameUtils;
import edu.passwordStorrager.utils.platform.PlatformUtils;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

import static edu.passwordStorrager.utils.FrameUtils.getCurrentClassName;

public class AboutApplication extends JFrame {
    private static final Logger log = Logger.getLogger(getCurrentClassName());
    private JPanel contentPane;
    private JLabel icon;
    private JLabel version;
    private JButton buttonOK;

    public AboutApplication() {
        setTitle("О программе");
        setContentPane(contentPane);
        setIconImage(PlatformUtils.appIcon);
        setResizable(false);
        icon.setSize(128,128);
        BufferedImage img = null;
        try {
            img = ImageIO.read(getClass().getResource("/resources/icons/icon_black_256.png"));
            Image dimg =  img.getScaledInstance(icon.getWidth(), icon.getHeight(),
                    Image.SCALE_SMOOTH);
            icon.setIcon(new ImageIcon(dimg));
        } catch (IOException e) {
            log.warn("Can not load file: /resources/icons/icon_black_256.png");
        }

        version.setText(version.getText()+ Application.APPLICATION_VERSION);
        pack();
        setPreferredSize(new Dimension(getWidth(), getHeight() + 20));
        pack();
        setLocation(FrameUtils.setFrameOnCenter(getSize()));
        setVisible(true);
    }
}
