package edu.passwordStorrager.gui;

import edu.passwordStorrager.core.Application;
import edu.passwordStorrager.utils.FrameUtils;
import edu.passwordStorrager.utils.StringUtils;
import edu.passwordStorrager.utils.platform.MacOsXUtils;
import edu.passwordStorrager.utils.platform.PlatformUtils;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import static edu.passwordStorrager.utils.FrameUtils.getCurrentClassName;

public class AboutApplication extends JFrame {
    private static final Logger log = Logger.getLogger(getCurrentClassName());
    private JPanel contentPane;
    private JLabel icon;
    private JLabel version;
    private JLabel name;
    private JButton buttonOK;
    private boolean isJeSuisDonbassActive = false;

    public AboutApplication() {
        setTitle("О программе");
        setContentPane(contentPane);
        if (!MacOsXUtils.isBundled()) {
            setIconImage(PlatformUtils.appIcon);
        }
        setResizable(false);
        icon.setSize(128, 128);
        icon.setIcon(FrameUtils.resizeIcon(getClass().getResource("/resources/icons/icon_black_256.png"), icon.getSize()));

        final String original = version.getText() + Application.APPLICATION_VERSION;
        version.setText(original);
        addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {

                if (e.getButton() == MouseEvent.BUTTON1) {
                    if (e.getClickCount() == 2) {
                        if (isJeSuisDonbassActive) {
                            String site = "http://www.google.ru/search?q=donbass+war+people&newwindow=1&es_sm=119&qscrl=1&tbm=isch&tbo=u&source=univ&sa=X&ei=_HfaVOjCCoH9ywPY3ICQAg&ved=0CCMQsAQ&biw=1180&bih=598#imgdii=_";
                            StringUtils.openWebPage(site);
                        }
                    }
                }
                if (e.getButton() == MouseEvent.BUTTON3) {
                    isJeSuisDonbassActive = !isJeSuisDonbassActive;
                    if (!isJeSuisDonbassActive) {
                        name.setText(Application.APPLICATION_NAME);
                        icon.setIcon(FrameUtils.resizeIcon(getClass().getResource("/resources/icons/icon_black_256.png"), icon.getSize()));
                    } else {
                        name.setText("Je Suis Donbass");
                        icon.setIcon(FrameUtils.resizeIcon(getClass().getResource("/resources/icons/JSD/saveDonbassPeople.png"), icon.getSize()));
                    }
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

}
