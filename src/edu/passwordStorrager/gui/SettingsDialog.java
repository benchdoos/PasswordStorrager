package edu.passwordStorrager.gui;

import edu.passwordStorrager.core.Main;
import edu.passwordStorrager.protector.Protector;
import edu.passwordStorrager.core.PropertiesManager;
import edu.passwordStorrager.objects.Key;
import edu.passwordStorrager.protector.Values;
import edu.passwordStorrager.utils.KeyUtils;
import edu.passwordStorrager.utils.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class SettingsDialog extends JDialog {

    String iCloudLogin = "";
    String iCloudPassword = "";
    String megaLogin = "";
    String megaPassword = "";
    String dropBoxLogin = "";
    String dropBoxPassword = "";

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
                d.setDirectory(Main.userHome);
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
                d.setDirectory(Main.userHome);
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
                    if (Main.key != null && !Main.key.getICloudLogin().isEmpty()) {
                        try {
                            login = Protector.decrypt(Main.key.getICloudLogin());
                            password = Protector.decrypt(Main.key.getICloudPassword());
                        } catch (GeneralSecurityException | IOException ignored) {
                        }
                    }
                } else {
                    login = iCloudLogin;
                    password = iCloudPassword;
                }
                new AccountEnterDialog("iCloud", login, password) {
                    @Override
                    void onOK() {
                        if (!this.textField1.getText().isEmpty() && !new String(this.passwordField1.getPassword()).isEmpty()) {
                            iCloudLogin = this.textField1.getText();
                            iCloudPassword = new String(this.passwordField1.getPassword());
                            dispose();
                        }
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
                        if (!this.textField1.getText().isEmpty() && !new String(this.passwordField1.getPassword()).isEmpty()) {
                            megaLogin = this.textField1.getText();
                            megaPassword = new String(this.passwordField1.getPassword());
                            dispose();
                        }
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
                        if (!this.textField1.getText().isEmpty() && !new String(this.passwordField1.getPassword()).isEmpty()) {
                            dropBoxLogin = this.textField1.getText();
                            dropBoxPassword = new String(this.passwordField1.getPassword());
                            dispose();
                        }
                    }
                };
            }
        });

        pack();
        setVisible(true);

    }

    private void onOK() {

        Key key = new Key();

        if (validPath(keyField.getText()) && validPath(storageField.getText())) {
            PropertiesManager.changeProperties(StringUtils.fixFolder(keyField.getText()), StringUtils.fixFolder(storageField.getText()));
            Main.properties = PropertiesManager.loadProperties();
        }

        if (!iCloudLogin.isEmpty() && !iCloudPassword.isEmpty()) {
            key.setICloud(iCloudLogin, iCloudPassword);
        } else {
            if (!Main.key.getICloudLogin().isEmpty() && !Main.key.getICloudPassword().isEmpty()) {
                key.setICloud(Main.key.getICloudLogin(), Main.key.getICloudPassword());
            }
        }
        if (!megaLogin.isEmpty() && !megaPassword.isEmpty()) {
            key.setMega(megaLogin, megaPassword);
        } else {
            if (!Main.key.getMegaLogin().isEmpty() && !Main.key.getMegaPassword().isEmpty()) {
                key.setICloud(Main.key.getMegaLogin(), Main.key.getMegaPassword());
            }
        }
        if (!dropBoxLogin.isEmpty() && !dropBoxPassword.isEmpty()) {
            key.setDropBox(dropBoxLogin, dropBoxPassword);
        } else {
            if (!Main.key.getDropBoxLogin().isEmpty() && !Main.key.getDropBoxPassword().isEmpty()) {
                key.setICloud(Main.key.getDropBoxLogin(), Main.key.getDropBoxPassword());
            }
        }
        String keyFilePath = Main.properties.getProperty(PropertiesManager.KEY_NAME) + Values.DEFAULT_KEY_FILE_NAME;
        try {
            key.encrypt();
            key.setENC(Main.key.getENC());
            KeyUtils.createKeyFile(key, keyFilePath);
            Main.key = KeyUtils.loadKeyFile(keyFilePath);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            System.err.println("Can not create file: " + keyFilePath);
        }

        dispose();
    }

    private void onCancel() {
// add your code here if necessary
        dispose();
    }

    private boolean validPath(String path) {
        return new File(path).exists() && new File(path).isDirectory();
    }
}
