package edu.passwordStorrager.gui;

import edu.passwordStorrager.cloud.CloudManager;
import edu.passwordStorrager.core.Core;
import edu.passwordStorrager.core.PasswordStorrager;
import edu.passwordStorrager.protector.Encryption;
import edu.passwordStorrager.protector.Protector;
import edu.passwordStorrager.protector.Values;
import edu.passwordStorrager.utils.FrameUtils;
import edu.passwordStorrager.utils.platform.MacOsXUtils;
import edu.passwordStorrager.utils.platform.PlatformUtils;
import edu.passwordStorrager.xmlManager.XmlParser;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Locale;

import static edu.passwordStorrager.core.Application.IS_MAC;
import static edu.passwordStorrager.core.PasswordStorrager.isUnlocked;
import static edu.passwordStorrager.core.PasswordStorrager.propertiesApplication;
import static edu.passwordStorrager.core.PropertiesManager.*;
import static edu.passwordStorrager.utils.FileUtils.exists;
import static edu.passwordStorrager.utils.FrameUtils.*;

public class AuthorizeDialog extends JDialog {
    private static final Logger log = Logger.getLogger(getCurrentClassName());

    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JPasswordField passwordField;
    private JProgressBar progressBar;
    private JLabel languageLabel;
    private JPanel languagePanel;
    private Timer timer;
    public static boolean isBlocked = false;

    public AuthorizeDialog(boolean isModal) {
        isBlocked = false;
        isUnlocked = false;
        initTimer();
        setContentPane(contentPane);
        if (!MacOsXUtils.isBundled()) {
            setIconImage(PlatformUtils.appIcon);
        }
        setTitle("Вход");
        setModal(isModal);
        setResizable(false);
        progressBar.setIndeterminate(true);
        progressBar.putClientProperty("JProgressBar.style", "circular");

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                buttonOK.setVisible(false);
                progressBar.setVisible(true);
                
                passwordField.setEnabled(false);
                buttonCancel.setEnabled(false);
                
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

        passwordField.registerKeyboardAction(deletePasswordActionListener,
                getKeyStrokeForOS(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, InputEvent.META_MASK),
                        KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, InputEvent.CTRL_MASK)), 
                JComponent.WHEN_FOCUSED);

        passwordField.getDocument().addDocumentListener(new DocumentListener() {
            void updateDefaultButton() {
                if (new String(passwordField.getPassword()).isEmpty()) {
                    getRootPane().setDefaultButton(null);
                } else {
                    getRootPane().setDefaultButton(buttonOK);
                }
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateDefaultButton();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateDefaultButton();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {

            }
        });

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
        }, getKeyStrokeForOS(KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.META_MASK),
                KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_MASK)), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        passwordField.registerKeyboardAction(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                passwordField.requestFocus();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        
        
        passwordField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                
                
            }
        });
        
        Timer timer1 = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Window w = FrameUtils.getWindows(AuthorizeDialog.class).get(0);
                Locale locale = w.getInputContext().getLocale();
                String name = locale.getLanguage().toUpperCase();
                if(!name.isEmpty()) {
                    languageLabel.setText(name);
                    languagePanel.setToolTipText(locale.getDisplayLanguage());
                } else {
                    languageLabel.setText("*");
                    languagePanel.setToolTipText("Незнакомая раскладка");
                }
            }
        });
        timer1.setRepeats(true);
        timer1.start();

        pack();

        setLocation(FrameUtils.setFrameOnCenter(getSize()));
        if (IS_MAC && !isAnyMainFormNoticed()) {
            com.apple.eawt.Application.getApplication().requestUserAttention(true);
        }
        //TODO request foreground here if is in settings??
        FrameUtils.registerWindow(this);
        setVisible(true);
    }

    private boolean isAnyMainFormNoticed() {
        for (Window w : PasswordStorrager.frames) {
            if (w instanceof MainForm) {
                return true;
            }
        }
        return false;
    }

    private void onOK() {
        String hexedPassword = Protector.hexPassword(Protector.hexPassword(new String(passwordField.getPassword())));
        Protector.PASSWORD = hexedPassword.toCharArray();

        if (exists(propertiesFilePath)) {
            propertiesApplication = loadProperties(propertiesFilePath);
            if (isCorrect()) {
                buttonCancel.setEnabled(true);
                buttonCancel.requestFocus();
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
