package edu.passwordStorrager.gui;

import edu.passwordStorrager.core.Application;
import edu.passwordStorrager.utils.FrameUtils;
import edu.passwordStorrager.utils.platform.PlatformUtils;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import static edu.passwordStorrager.utils.FrameUtils.getCurrentClassName;

public class AboutApplication extends JFrame {
    private static final Logger log = Logger.getLogger(getCurrentClassName());
    private JPanel contentPane;
    private JLabel icon;
    private JLabel version;
    private JLabel name;
    private JButton buttonOK;
    private boolean isJeSuisDonbassActive = true;

    public AboutApplication() {
        setTitle("О программе");
        setContentPane(contentPane);
        setIconImage(PlatformUtils.appIcon);
        setResizable(false);
        icon.setSize(128, 128);
        setNewIcon(getClass().getResource("/resources/icons/icon_black_256.png"));

        final String original = version.getText() + Application.APPLICATION_VERSION;
        version.setText(original);
        addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    if (!isJeSuisDonbassActive) {
                        name.setText(Application.APPLICATION_NAME);
                        setNewIcon(getClass().getResource("/resources/icons/icon_black_256.png"));
                    } else {
                        name.setText("Je Suis Donbass");
                        setNewIcon(getClass().getResource("/resources/icons/JSD/saveDonbassPeople.png"));
                    }
                    isJeSuisDonbassActive = !isJeSuisDonbassActive;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });
        pack();
        setPreferredSize(new Dimension(getWidth(), getHeight() + 20));
        pack();
        setLocation(FrameUtils.setFrameOnCenter(getSize()));
        setVisible(true);
    }

    private void setNewIcon(URL url) {
        try {
            BufferedImage img = ImageIO.read(url);
            Image scaledImage = img.getScaledInstance(icon.getWidth(), icon.getHeight(),
                    Image.SCALE_SMOOTH);
            icon.setIcon(new ImageIcon(scaledImage));
        } catch (IOException e) {
            log.warn("Can not load file: /resources/icons/icon_black_256.png");
        }

        version.setText(version.getText() + Application.APPLICATION_VERSION);
        pack();
        setPreferredSize(new Dimension(getWidth(), getHeight() + 20));
        pack();
        setLocation(FrameUtils.setFrameOnCenter(getSize()));
        setVisible(true);
    }
}
