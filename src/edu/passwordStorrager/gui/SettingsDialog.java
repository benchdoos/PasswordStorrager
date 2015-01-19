package edu.passwordStorrager.gui;

import edu.passwordStorrager.core.Main;
import edu.passwordStorrager.core.PropertiesManager;
import edu.passwordStorrager.objects.Key;
import edu.passwordStorrager.protector.Protector;
import edu.passwordStorrager.protector.Values;
import edu.passwordStorrager.utils.FrameUtils;
import edu.passwordStorrager.utils.KeyUtils;
import edu.passwordStorrager.utils.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

import static edu.passwordStorrager.utils.FrameUtils.getCurrentClassName;
import static edu.passwordStorrager.utils.FrameUtils.getFrameLocation;
import static edu.passwordStorrager.utils.FrameUtils.getFrameSize;

public abstract class SettingsDialog extends JDialog {

    String iCloudLogin = "";
    String iCloudPassword = "";
    String megaLogin = "";
    String megaPassword = "";
    String dropBoxLogin = "";
    String dropBoxPassword = "";

    boolean isICloudChanged = false, isMegaChanged = false, isDropBoxChanged = false;

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

    public SettingsDialog() {
        setResizable(false);
        setMinimumSize(new Dimension(380,320));

        setPreferredSize(getFrameSize(getCurrentClassName()));
        setLocation(getFrameLocation(getCurrentClassName()));

        addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent e) {
                FrameUtils.setFrameLocation(getClass().getEnclosingClass().getName(), getLocation());
                //FrameUtils.setFrameSize(getClass().getEnclosingClass().getName(), getSize());
            }
        });

        storageField.setText(Main.properties.getProperty("Storage"));
        keyField.setText(Main.properties.getProperty("Key"));

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame frame = new JFrame();

                FileDialog d = new FileDialog(frame);
                d.setDirectory(Main.USER_HOME);
                d.setFilenameFilter(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return new File(dir.getAbsolutePath() + name).isDirectory();
                    }
                });
                d.setVisible(true);
                storageField.setText(d.getDirectory() + d.getFile());
            }
        });

        browseKeyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame frame = new JFrame();

                FileDialog d = new FileDialog(frame);
                d.setDirectory(Main.USER_HOME);
                d.setFilenameFilter(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return new File(dir.getAbsolutePath() + name).isDirectory();
                    }
                });
                d.setVisible(true);
                keyField.setText(d.getDirectory() + d.getFile());
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
                    if (Main.key != null) {
                        System.out.println(Main.key);
                        if (!Main.key.getICloudLogin().isEmpty()) {
                            try {
                                login = Protector.decrypt(Main.key.getICloudLogin());
                                password = Protector.decrypt(Main.key.getICloudPassword());
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
                    if (Main.key != null && !Main.key.getMegaLogin().isEmpty()) {
                        try {
                            login = Protector.decrypt(Main.key.getMegaLogin());
                            password = Protector.decrypt(Main.key.getMegaPassword());
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
                    if (Main.key != null && !Main.key.getDropBoxLogin().isEmpty()) {
                        try {
                            login = Protector.decrypt(Main.key.getDropBoxLogin());
                            password = Protector.decrypt(Main.key.getDropBoxPassword());
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

        pack();
        setVisible(true);

    }

    abstract public void onOK();

    public void saveSettings() {
        Key key = new Key();

        try {
            key = createKey(key);
            pushSettings(key);
        } catch (GeneralSecurityException | UnsupportedEncodingException e) {
            e.printStackTrace();
            System.err.println("Can not encrypt data to save to key file");
        }

    }

    private void pushSettings(Key key) {
        String keyFilePath = Main.properties.getProperty(PropertiesManager.KEY_NAME) + Values.DEFAULT_KEY_FILE_NAME;
        try {//doos, проверить входящие данные, затем по очереди пихать в настройки а потом уже писать все это дело
            // сделать key.setEncrypted(true) и иже потом закидывать зашифрованные данные
            key.setENC(Main.key.getENC());
            key.setEncrypted(true);
            KeyUtils.createKeyFile(key, keyFilePath);
            Main.key = KeyUtils.loadKeyFile(keyFilePath);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            System.err.println("Can not create file: " + keyFilePath);
        }
    }

    private Key createKey(Key key) throws GeneralSecurityException, UnsupportedEncodingException {
        //TODO divide this into two checks and methods
        if (StringUtils.validPath(keyField.getText()) && StringUtils.validPath(storageField.getText())) {
            PropertiesManager.changeProperties(StringUtils.fixFolder(keyField.getText()), StringUtils.fixFolder(storageField.getText()));
            Main.properties = PropertiesManager.loadProperties(PropertiesManager.propertiesFilePath);
        }

        if (isICloudChanged) {
            key.setICloud(Protector.encrypt(iCloudLogin), Protector.encrypt(iCloudPassword));
        } else {
            if (!Main.key.getICloudLogin().isEmpty() && !Main.key.getICloudPassword().isEmpty()) {
                key.setICloud(Main.key.getICloudLogin(), Main.key.getICloudPassword());
            }
        }
        if (isMegaChanged) {
            key.setMega(Protector.encrypt(megaLogin), Protector.encrypt(megaPassword));
        } else {
            if (!Main.key.getMegaLogin().isEmpty() && !Main.key.getMegaPassword().isEmpty()) {
                key.setMega(Main.key.getMegaLogin(), Main.key.getMegaPassword());
            }
        }
        if (isDropBoxChanged) {
            key.setDropBox(Protector.encrypt(dropBoxLogin), Protector.encrypt(dropBoxPassword));
        } else {
            if (!Main.key.getDropBoxLogin().isEmpty() && !Main.key.getDropBoxPassword().isEmpty()) {
                key.setDropBox(Main.key.getDropBoxLogin(), Main.key.getDropBoxPassword());
            }
        }
        return key;
    }

    private void onCancel() {
        dispose();
    }

}
