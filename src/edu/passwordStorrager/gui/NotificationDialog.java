package edu.passwordStorrager.gui;

import edu.passwordStorrager.utils.FrameUtils;
import edu.passwordStorrager.utils.platform.PlatformUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public abstract class NotificationDialog extends JDialog {
    public static final int NOTIFICATION_INFO = 1, NOTIFICATION_ERROR = -2, NOTIFICATION_WARN = -1;

    private JPanel contentPane;
    private JButton buttonOK;
    private JLabel titleLabel;
    private JLabel iconLabel;
    private JTextPane messageLabel;

    String title, message;
    int type;

    public NotificationDialog(String title, String message, int type) {
        this.title = title;
        this.message = message;
        this.type = type;
        /*if (!Application.IS_MAC) {
            init();
        } else {
            System.out.println("NOTIF: " + type + " " + title + " " + message);
            //TODO send notification here
        }*/
        init();
    }

    private void init() {
        setContentPane(contentPane);
        setIconImage(PlatformUtils.appIcon);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        setMinimumSize(new Dimension(300, 200));
        setMaximumSize(new Dimension(400, 250));

        titleLabel.setText(title);
        messageLabel.setContentType("text/html");
        messageLabel.setText(message);

        switch (type) {
            case NOTIFICATION_INFO:
                setTitle("Информация");
                iconLabel.setIcon(UIManager.getIcon("OptionPane.informationIcon"));
                break;
            case NOTIFICATION_ERROR:
                setTitle("Ошибка");
                iconLabel.setIcon(UIManager.getIcon("OptionPane.errorIcon"));
                break;
            case NOTIFICATION_WARN:
                setTitle("Внимание");
                iconLabel.setIcon(UIManager.getIcon("OptionPane.warningIcon"));
                break;
            default:
                iconLabel.setIcon(UIManager.getIcon("OptionPane.informationIcon"));
                break;
        }

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        // call onOK() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onOK();
            }
        });

// call onOK() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        pack();
        setLocation(FrameUtils.setFrameOnCenter(getSize()));
        setVisible(true);
    }

    abstract public void onOK();

}
