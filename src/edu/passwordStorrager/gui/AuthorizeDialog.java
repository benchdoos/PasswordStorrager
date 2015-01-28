package edu.passwordStorrager.gui;

import edu.passwordStorrager.cloud.CloudManager;
import edu.passwordStorrager.core.Main;
import edu.passwordStorrager.protector.Encryption;
import edu.passwordStorrager.protector.Protector;
import edu.passwordStorrager.protector.Values;
import edu.passwordStorrager.utils.FrameUtils;
import edu.passwordStorrager.xmlManager.XmlParser;

import javax.swing.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

import static edu.passwordStorrager.core.PropertiesManager.*;
import static edu.passwordStorrager.utils.FileUtils.exists;

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

        //buttonOK.putClientProperty("JButton.buttonType", "textured"); //works))

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


        setLocation(FrameUtils.setFrameOnCenter(getSize()));
        Main.application.requestUserAttention(true);
        //TODO request foreground here if is in settings??
        Main.framesAuthForm.add(this);
        setVisible(true);
    }

    private void onOK() throws IOException {
        String hexedPassword = Protector.hexPassword(Protector.hexPassword(new String(passwordField.getPassword())));
        Protector.PASSWORD = hexedPassword.toCharArray();

        if (exists(propertiesFilePath)) {
            Main.propertiesApplication = loadProperties(propertiesFilePath);
            if (isCorrect()) {
                showProperties(Main.propertiesApplication);
                //TODO send notification here.
                System.out.println("Password is correct");
                Main.isAuthorized = true;
                Encryption.extractKey(new File(Main.propertiesApplication.getProperty(KEY_NAME) + Values.DEFAULT_KEY_FILE_NAME));
                
                new CloudManager().synchronize();
                
                new MainForm(new XmlParser().parseRecords());
            } else {
                //TODO send notification here.
                System.out.println("Password is not correct");
                Main.isAuthorized = false;
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
