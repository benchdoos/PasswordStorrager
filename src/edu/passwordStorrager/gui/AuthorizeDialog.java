package edu.passwordStorrager.gui;

import edu.passwordStorrager.cloud.CloudManager;
import edu.passwordStorrager.core.Core;
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

import static edu.passwordStorrager.core.Application.IS_MAC;
import static edu.passwordStorrager.core.Application.IS_WINDOWS;
import static edu.passwordStorrager.core.PasswordStorrager.*;
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
    private Timer timer;
    public static boolean isBlocked = false;

    public AuthorizeDialog() {
        isBlocked = false;
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
                isBlocked = true;
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

        if (IS_MAC) {
            passwordField.registerKeyboardAction(deletePasswordActionListener,
                    KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, InputEvent.META_MASK), JComponent.WHEN_FOCUSED);
        } else if (IS_WINDOWS) {
            passwordField.registerKeyboardAction(deletePasswordActionListener,
                    KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, InputEvent.CTRL_MASK), JComponent.WHEN_FOCUSED);
        }

// call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (!isBlocked) {
                    onCancel();
                }
            }
        });

// call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!isBlocked) {
                    onCancel();
                }
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        pack();

        setLocation(FrameUtils.setFrameOnCenter(getSize()));
        if (IS_MAC) {
            com.apple.eawt.Application.getApplication().requestUserAttention(true);
        }
        //TODO request foreground here if is in settings??
        frames.add(this);
        isUnlocked = false;
        setVisible(true);
    }

    private void onOK() {
        String hexedPassword = Protector.hexPassword(Protector.hexPassword(new String(passwordField.getPassword())));
        Protector.PASSWORD = hexedPassword.toCharArray();

        if (exists(propertiesFilePath)) {
            propertiesApplication = loadProperties(propertiesFilePath);
            if (isCorrect()) {

                System.out.println("Password is correct");
                setModal(false);
                if (FrameUtils.getWindows(MainForm.class).size() > 0) {
                    FrameUtils.getWindows(MainForm.class).get(0).setVisible(true);
                } else {
                    showProperties(propertiesApplication);
                    Encryption.extractKey(new File(propertiesApplication.getProperty(KEY_NAME) + Values.DEFAULT_KEY_FILE_NAME));

                    //TODO send notification here.
                    new CloudManager().synchronize();

                    MainForm mf = new MainForm(new XmlParser().parseRecords());
                    setVisible(false);
                    mf.setVisible(true);
                }
                isUnlocked = true;
                dispose();
            } else {
                //TODO send notification here.
                System.out.println("Password is not correct");
                isUnlocked = false;

                buttonOK.setEnabled(false);
                buttonOK.setVisible(true);
                buttonCancel.setEnabled(false);
                progressBar.setVisible(false);

                initTimer();
                timer.restart();
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

        ActionListener timerListener = new ActionListener() {
            int counter = 0;

            @Override
            public void actionPerformed(ActionEvent e) {
                counter++;
                if (counter == 10) {
                    buttonOK.setText("OK");
                    buttonOK.setEnabled(true);
                    buttonCancel.setEnabled(true);
                    passwordField.setEnabled(true);
                    passwordField.requestFocus();
                    passwordField.setToolTipText("");
                    counter = 0;
                    isBlocked = false;
                    timer.stop();
                } else {
                    buttonOK.setEnabled(false);
                    isBlocked = true;
                    passwordField.setToolTipText("Неверный пароль");
                    buttonOK.setText(10 - counter + "");
                }
            }
        };
        timer = new Timer(1000, timerListener);
    }
}
