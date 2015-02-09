package edu.passwordStorrager.gui;

import edu.passwordStorrager.cloud.CloudManager;
import edu.passwordStorrager.core.Core;
import edu.passwordStorrager.core.Main;
import edu.passwordStorrager.protector.Encryption;
import edu.passwordStorrager.protector.Protector;
import edu.passwordStorrager.protector.Values;
import edu.passwordStorrager.utils.FrameUtils;
import edu.passwordStorrager.utils.platform.PlatformUtils;
import edu.passwordStorrager.xmlManager.XmlParser;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.event.*;
import java.io.File;

import static edu.passwordStorrager.core.PropertiesManager.*;
import static edu.passwordStorrager.utils.FileUtils.exists;
import static edu.passwordStorrager.utils.FrameUtils.getCurrentClassName;

public class AuthorizeDialog extends JDialog {
    private static final Logger log = Logger.getLogger(getCurrentClassName());

    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JPasswordField passwordField;
    private JProgressBar progressBar;
    private static Timer timer;

    public AuthorizeDialog() {
        initTimer();
        setContentPane(contentPane);
        setIconImage(PlatformUtils.appIcon);
        setTitle("Вход");
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        setResizable(false);
        setIconImage(PlatformUtils.appIcon);
        progressBar.setIndeterminate(true);
        progressBar.putClientProperty("JProgressBar.style", "circular");

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                passwordField.setEnabled(false);
                buttonOK.setVisible(false);
                progressBar.setVisible(true);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        onOK();
                    }
                }).start();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        ActionListener deletePasswordActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                passwordField.setText("");
            }
        };

        if (Main.IS_MAC) {
            passwordField.registerKeyboardAction(deletePasswordActionListener,
                    KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, InputEvent.META_MASK), JComponent.WHEN_FOCUSED);
        } else if (Main.IS_WINDOWS) {
            passwordField.registerKeyboardAction(deletePasswordActionListener,
                    KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, InputEvent.CTRL_MASK), JComponent.WHEN_FOCUSED);
        }

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
        if (Main.IS_MAC) {
            com.apple.eawt.Application.getApplication().requestUserAttention(true);
        }
        //TODO request foreground here if is in settings??
        Main.framesAuthForm.add(this);
        setVisible(true);
    }

    private void onOK() {
        String hexedPassword = Protector.hexPassword(Protector.hexPassword(new String(passwordField.getPassword())));
        Protector.PASSWORD = hexedPassword.toCharArray();

        if (exists(propertiesFilePath)) {
            Main.propertiesApplication = loadProperties(propertiesFilePath);
            if (isCorrect()) {
                showProperties(Main.propertiesApplication);
                System.out.println("Password is correct");
                Main.isAuthorized = true;
                Encryption.extractKey(new File(Main.propertiesApplication.getProperty(KEY_NAME) + Values.DEFAULT_KEY_FILE_NAME));

                //TODO send notification here.
                new CloudManager().synchronize();

                setModal(false);
                MainForm mf = new MainForm(new XmlParser().parseRecords());
                setVisible(false);
                mf.setVisible(true);
                dispose();
            } else {
                //TODO send notification here.
                System.out.println("Password is not correct");
                Main.isAuthorized = false;

                buttonOK.setEnabled(false);
                buttonOK.setVisible(true);
                progressBar.setVisible(false);

                timer.start();
                FrameUtils.shakeFrame(this);
            }
        }
    }

    private void onCancel() {
// add your code here if necessary
        dispose();
        Core.onQuit();
    }

    private void initTimer() {

//        ActionListener timerListener =

        if (timer == null) {
            timer = new Timer(1000, new ActionListener() {
                int counter = 0;

                @Override
                public void actionPerformed(ActionEvent e) {
                    counter++;
                    if (counter == 10) {
                        buttonOK.setText("OK");
                        buttonOK.setEnabled(true);
                        passwordField.setEnabled(true);
                        passwordField.requestFocus();
                        counter = 0;
                        timer.stop();
                    } else {
                        buttonOK.setEnabled(false);
                        buttonOK.setText(10 - counter + "");
                    }
                }
            });
        }
    }
}
