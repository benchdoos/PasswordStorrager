package edu.passwordStorrager.gui;

import edu.passwordStorrager.core.PasswordStorrager;
import edu.passwordStorrager.core.PropertiesManager;
import edu.passwordStorrager.objects.Key;
import edu.passwordStorrager.protector.Protector;
import edu.passwordStorrager.protector.Values;
import edu.passwordStorrager.utils.FileUtils;
import edu.passwordStorrager.utils.FrameUtils;
import edu.passwordStorrager.utils.KeyUtils;
import edu.passwordStorrager.utils.StringUtils;
import edu.passwordStorrager.utils.platform.PlatformUtils;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

import static edu.passwordStorrager.core.Application.APPLICATION_NAME;
import static edu.passwordStorrager.core.Application.USER_HOME;
import static edu.passwordStorrager.utils.FrameUtils.*;

public abstract class SettingsDialog extends JDialog {
    private static final Logger log = Logger.getLogger(getCurrentClassName());

    private String iCloudLogin = "";
    private String iCloudPassword = "";
    private String megaLogin = "";
    private String megaPassword = "";
    private String dropBoxLogin = "";
    private String dropBoxPassword = "";

    private boolean isICloudChanged = false;
    private boolean isMegaChanged = false;
    private boolean isDropBoxChanged = false;

    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JButton browseButton;
    private JButton button2;
    private JButton button1;
    private JButton button3;
    private JTextField keyField;
    private JButton browseKeyButton;
    private JTextField storageField;
    private JButton changeKey;
    private static boolean isCreated = false;

    public SettingsDialog() {
        if (!isCreated) {
            init();
            isCreated = true;
            setVisible(true);
        }
    }

    private void init() {
        setResizable(false);
        setMinimumSize(new Dimension(380, 320));
        setIconImage(PlatformUtils.appIcon);

        setTitle("Настройки " + APPLICATION_NAME);

        setPreferredSize(getFrameSize(getCurrentClassName()));
        setLocation(getFrameLocation(getCurrentClassName()));

        addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent e) {
                FrameUtils.setFrameLocation(getClass().getEnclosingClass().getName(), getLocation());
                //FrameUtils.setFrameSize(getClass().getEnclosingClass().getName(), getSize());
            }
        });

        storageField.setText(PasswordStorrager.propertiesApplication.getProperty("Storage"));
        keyField.setText(PasswordStorrager.propertiesApplication.getProperty("Key"));

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame frame = new JFrame();

                FileDialog d = new FileDialog(frame);
                d.setDirectory(USER_HOME);
                d.setFilenameFilter(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return new File(dir.getAbsolutePath() + name).isDirectory();
                    }
                });
                d.setVisible(true);
                storageField.setText(d.getDirectory() != null ? d.getDirectory() + d.getFile() : "");
            }
        });

        browseKeyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame frame = new JFrame();

                FileDialog d = new FileDialog(frame);
                d.setDirectory(USER_HOME);
                d.setFilenameFilter(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return new File(dir.getAbsolutePath() + name).isDirectory();
                    }
                });
                d.setVisible(true);
                keyField.setText(d.getDirectory() != null ? d.getDirectory() + d.getFile() : "");
            }
        });

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
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

        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String login = "", password = "";
                if (iCloudLogin.isEmpty()) {
                    if (PasswordStorrager.key != null) {
                        if (!PasswordStorrager.key.getICloudLogin().isEmpty()) {
                            try {
                                login = Protector.decrypt(PasswordStorrager.key.getICloudLogin());
                                password = Protector.decrypt(PasswordStorrager.key.getICloudPassword());
                            } catch (GeneralSecurityException | IOException ignored) {
                            }
                        }
                    }
                } else {
                    login = iCloudLogin;
                    password = iCloudPassword;
                }
                new AccountEnterDialog("iCloud", login, password) {
                    @Override
                    void onOK() {
                        isICloudChanged = true;
                        iCloudLogin = this.textField1.getText();
                        iCloudPassword = new String(this.passwordField1.getPassword());
                        dispose();

                    }
                };
            }
        });

        button2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String login = "", password = "";
                if (megaLogin.isEmpty()) {
                    if (PasswordStorrager.key != null && !PasswordStorrager.key.getMegaLogin().isEmpty()) {
                        try {
                            login = Protector.decrypt(PasswordStorrager.key.getMegaLogin());
                            password = Protector.decrypt(PasswordStorrager.key.getMegaPassword());
                        } catch (GeneralSecurityException | IOException ignored) {
                        }
                    }
                } else {
                    login = megaLogin;
                    password = megaPassword;
                }
                new AccountEnterDialog("MEGA", login, password) {
                    @Override
                    void onOK() {
                        isMegaChanged = true;
                        megaLogin = this.textField1.getText();
                        megaPassword = new String(this.passwordField1.getPassword());
                        dispose();
                    }
                };
            }
        });

        button3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String login = "", password = "";
                if (dropBoxLogin.isEmpty()) {
                    if (PasswordStorrager.key != null && !PasswordStorrager.key.getDropBoxLogin().isEmpty()) {
                        try {
                            login = Protector.decrypt(PasswordStorrager.key.getDropBoxLogin());
                            password = Protector.decrypt(PasswordStorrager.key.getDropBoxPassword());
                        } catch (GeneralSecurityException | IOException ignored) {
                        }
                    }
                } else {
                    login = dropBoxLogin;
                    password = dropBoxPassword;
                }
                new AccountEnterDialog("DropBox", login, password) {
                    @Override
                    void onOK() {
                        isDropBoxChanged = true;
                        dropBoxLogin = this.textField1.getText();
                        dropBoxPassword = new String(this.passwordField1.getPassword());
                        dispose();
                    }
                };
            }
        });

        changeKey.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                new ChangeKey() {
                    @Override
                    public void onOK() {
                        saveOptions();
                        dispose();
                    }
                };
            }
        });

        pack();
    }

    abstract public void onOK();

    public void saveSettings() {
        Key key = new Key();

        try {
            key = createKey(key);
            pushSettings(key);
        } catch (GeneralSecurityException | UnsupportedEncodingException e) {
            log.warn("Can not encrypt data to save to key file", e);
        }

    }

    private void pushSettings(Key key) {
        String keyFilePath = PasswordStorrager.propertiesApplication.getProperty(PropertiesManager.KEY_NAME) + Values.DEFAULT_KEY_FILE_NAME;
        try {
            key.setENC(PasswordStorrager.key.getENC());
            KeyUtils.createKeyFile(key, keyFilePath);
            PasswordStorrager.key = KeyUtils.loadKeyFile(keyFilePath);
        } catch (Throwable e) {
            log.warn("Can not create file: " + keyFilePath, e);
        }
    }

    private Key createKey(Key key) throws GeneralSecurityException, UnsupportedEncodingException {
        //TODO divide this into two checks and methods
        if (FileUtils.validPath(keyField.getText()) && FileUtils.validPath(storageField.getText())) {
            PropertiesManager.changeProperties(StringUtils.fixFolder(keyField.getText()), StringUtils.fixFolder(storageField.getText()));
            PasswordStorrager.propertiesApplication = PropertiesManager.loadProperties(PropertiesManager.propertiesFilePath);
        }

        if (isICloudChanged) {
            key.setICloud(Protector.encrypt(iCloudLogin), Protector.encrypt(iCloudPassword));
        } else {
            if (!PasswordStorrager.key.getICloudLogin().isEmpty() && !PasswordStorrager.key.getICloudPassword().isEmpty()) {
                key.setICloud(PasswordStorrager.key.getICloudLogin(), PasswordStorrager.key.getICloudPassword());
            }
        }
        if (isMegaChanged) {
            key.setMega(Protector.encrypt(megaLogin), Protector.encrypt(megaPassword));
        } else {
            if (!PasswordStorrager.key.getMegaLogin().isEmpty() && !PasswordStorrager.key.getMegaPassword().isEmpty()) {
                key.setMega(PasswordStorrager.key.getMegaLogin(), PasswordStorrager.key.getMegaPassword());
            }
        }
        if (isDropBoxChanged) {
            key.setDropBox(Protector.encrypt(dropBoxLogin), Protector.encrypt(dropBoxPassword));
        } else {
            if (!PasswordStorrager.key.getDropBoxLogin().isEmpty() && !PasswordStorrager.key.getDropBoxPassword().isEmpty()) {
                key.setDropBox(PasswordStorrager.key.getDropBoxLogin(), PasswordStorrager.key.getDropBoxPassword());
            }
        }
        return key;
    }

    private void onCancel() {
        isCreated = false;
        dispose();
    }

}
