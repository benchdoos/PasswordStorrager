package edu.passwordStorrager.gui;

import edu.passwordStorrager.core.Main;
import edu.passwordStorrager.protector.Protector;
import edu.passwordStorrager.core.PropertiesManager;
import edu.passwordStorrager.objects.Key;
import edu.passwordStorrager.objects.Record;
import edu.passwordStorrager.protector.Values;
import edu.passwordStorrager.utils.KeyUtils;
import edu.passwordStorrager.utils.StringUtils;
import edu.passwordStorrager.xmlManager.XmlParser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

public class FirstLaunchDialog extends JDialog {

    String iCloudLogin = "";
    String iCloudPassword = "";
    String megaLogin = "";
    String megaPassword = "";
    String dropBoxLogin = "";
    String dropBoxPassword = "";


    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField storageField;
    private JButton browseButton;
    private JButton button2;
    private JButton button1;
    private JPasswordField passwordField1;
    private JButton button3;
    private JButton browseKeyButton;
    private JTextField keyField;

    public FirstLaunchDialog() {
        setResizable(false);
        /*storageField.setText(PropertiesManager.properties.getProperty("Storage"));*/

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
                storageField.setText(d.getDirectory() + d.getFile() + File.separator);
            }
        });

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

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
                new AccountEnterDialog("iCloud", iCloudLogin, iCloudPassword) {
                    @Override
                    void onOK() {
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
                new AccountEnterDialog("MEGA", megaLogin, megaPassword) {
                    @Override
                    void onOK() {
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
                new AccountEnterDialog("DropBox", dropBoxLogin, dropBoxPassword) {
                    @Override
                    void onOK() {
                        dropBoxLogin = this.textField1.getText();
                        dropBoxPassword = new String(this.passwordField1.getPassword());
                        dispose();
                    }
                };
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
                keyField.setText(d.getDirectory() + d.getFile() + File.separator);
            }
        });

        pack();
        int width = (Toolkit.getDefaultToolkit().getScreenSize().width / 2) - (this.getWidth() / 2);
        int height = (Toolkit.getDefaultToolkit().getScreenSize().height / 2) - (this.getHeight() / 2);
        setLocation(width, height);
        setVisible(true);
    }


    private void onOK() {
        if (!new String(passwordField1.getPassword()).isEmpty()) {
            if (validatePath(keyField.getText()) && validatePath(storageField.getText())) {
                registerNewStorage();
                try {
                    Main.key = KeyUtils.loadKeyFile(StringUtils.fixFolder(keyField.getText()) + Values.DEFAULT_KEY_FILE_NAME);
                } catch (Throwable throwable) {
                    System.err.println("Can not load key file: " + StringUtils.fixFolder(keyField.getText()) + Values.DEFAULT_KEY_FILE_NAME);
                }
                dispose();
            }
        }
    }

    private void onCancel() {
// add your code here if necessary
        dispose();
        System.exit(0);
    }

    private boolean validatePath(String path) {
        return new File(path).isDirectory() && new File(path).exists();
    }

    private void registerNewStorage() {
        String hexedPassword = Protector.hexPassword(Protector.hexPassword(new String(passwordField1.getPassword())));
        Protector.PASSWORD = hexedPassword.toCharArray();

        try {
            createProperties(); //where storage is situated
            createEmptyStorage();
            try {
                createKeyFile();

                new MainForm(new XmlParser().parseRecords()).setVisible(true);
            } catch (Throwable throwable) {
                System.err.println("Can not create key");
            }
        } catch (Throwable e) {
            System.err.println("Can not create storage in: " + storageField.getText());
        }
    }

    private void createKeyFile() throws Throwable {

        Key key = new Key();
        key.setENC(Protector.hexPassword(Protector.hexPassword(new String(passwordField1.getPassword()))));//new String(passwordField1.getPassword())
        key.setICloud(iCloudLogin, iCloudPassword);
        key.setMega(megaLogin, megaPassword);
        key.setDropBox(dropBoxLogin, dropBoxPassword);
        key.encrypt();

        KeyUtils.createKeyFile(key, StringUtils.fixFolder(keyField.getText()) + Values.DEFAULT_KEY_FILE_NAME);
    }

    private void createProperties() {
        String key = StringUtils.fixFolder(keyField.getText());
        String storage = StringUtils.fixFolder(storageField.getText());

        PropertiesManager.changeProperties(key, storage);
    }

    private void createEmptyStorage() throws Throwable {
        File file = new File(StringUtils.fixFolder(storageField.getText()) + "storage");
        if (!file.exists()) {
            if (file.createNewFile()) {
                new XmlParser().saveRecords(new ArrayList<Record>());
            } else {
                throw new IOException("Can not create file: " + file.getAbsolutePath());
            }
        }
    }

}
