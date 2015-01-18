package edu.passwordStorrager.gui;

import edu.passwordStorrager.core.Main;
import edu.passwordStorrager.core.PasswordProtector;
import edu.passwordStorrager.core.PropertiesManager;
import edu.passwordStorrager.protector.DefaultValues;
import edu.passwordStorrager.protector.Encryption;
import edu.passwordStorrager.xmlManager.XmlParser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

public class AuthorizeDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JPasswordField passwordField;

    public AuthorizeDialog() {
        setResizable(false);

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    onOK();
                } catch (IOException e1) {
                    System.out.println("Can not load custom file, fatal");
                    System.exit(-1);
                }
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

// call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

// call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        pack();

        int width = (Toolkit.getDefaultToolkit().getScreenSize().width / 2) - (this.getWidth() / 2);
        int height = (Toolkit.getDefaultToolkit().getScreenSize().height / 2) - (this.getHeight() / 2);
        setLocation(width, height);

        setVisible(true);
    }

    private void onOK() throws IOException {
        String hexedPassword = PasswordProtector.hexPassword(PasswordProtector.hexPassword(new String(passwordField.getPassword())));
        PasswordProtector.PASSWORD = hexedPassword.toCharArray();

        if (PropertiesManager.exists()) {
            Main.properties = PropertiesManager.loadProperties();
            if (PropertiesManager.isCorrect()) {
                PropertiesManager.showProperties(Main.properties);
                System.out.println("Password is correct");
                //TODO send notification here.
                new Encryption(new File(Main.properties.getProperty(PropertiesManager.KEY_NAME) + DefaultValues.DEFAULT_KEY_FILE));
                new MainForm(new XmlParser().parseRecords()).setVisible(true);
            } else {
                //TODO send notification here.
                System.out.println("Password is not correct");
                dispose();
                System.exit(-1);
            }
        }
        dispose();
    }

    private void onCancel() {
// add your code here if necessary
        dispose();
        System.exit(0);
    }
}
