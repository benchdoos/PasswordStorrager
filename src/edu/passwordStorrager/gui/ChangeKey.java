package edu.passwordStorrager.gui;

import edu.passwordStorrager.core.PasswordStorrager;
import edu.passwordStorrager.core.PropertiesManager;
import edu.passwordStorrager.protector.Protector;
import edu.passwordStorrager.protector.Values;
import edu.passwordStorrager.utils.FileUtils;
import edu.passwordStorrager.utils.FrameUtils;
import edu.passwordStorrager.utils.StringUtils;
import edu.passwordStorrager.utils.platform.PlatformUtils;
import edu.passwordStorrager.xmlManager.XmlParser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;

public abstract class ChangeKey extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JPasswordField passwordField1;
    private JTextField textField1;
    private JButton button1;
    private JTextField textField2;
    private JButton button2;
    private JTextPane description;

    public ChangeKey() {
        setTitle("Указать хранилище");
        setIconImage(PlatformUtils.appIcon);
        description.setText("<html>Укажите местоположение файла ключа и хранилища. <br>" +
                "Укажите пароль, заданный при создании данного хранилища. </html>");
        setContentPane(contentPane);
        setModal(true);
        setResizable(false);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (checkFields()) {
                    onOK();
                } else {
                    Component button = (Component) e.getSource();
                    FrameUtils.shakeFrame(button);
                }
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = FrameUtils.getFolderChooser("Выбрать папку с ключем...");

                int result = fileChooser.showSaveDialog(null);
                if (result == JFileChooser.APPROVE_OPTION) {
                    String path = fileChooser.getSelectedFile().getAbsolutePath();
                    textField1.setText(path + File.separator);
                }

                /*JFrame frame = new JFrame();
                frame.getRootPane().putClientProperty("apple.awt.fileDialogForDirectories","true");
                FileDialog d = new FileDialog(frame);
                d.setDirectory(Application.USER_HOME);
                d.setFilenameFilter(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return new File(dir.getAbsolutePath() + name).isDirectory();
                    }
                });
                d.setVisible(true);
                textField1.setText(d.getDirectory() != null ? d.getDirectory() + d.getFile() : "");*/
            }
        });

        button2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = FrameUtils.getFolderChooser("Выбрать папку с хранилищем...");

                int result = fileChooser.showSaveDialog(null);
                if (result == JFileChooser.APPROVE_OPTION) {
                    String path = fileChooser.getSelectedFile().getAbsolutePath();
                    textField2.setText(path + File.separator);
                }
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
        setVisible(true);
    }

    public abstract void onOK();

    public boolean checkFields() {
        return !new String(passwordField1.getPassword()).isEmpty()
                && FileUtils.exists(StringUtils.fixFolder(textField1.getText()) + Values.DEFAULT_KEY_FILE_NAME)
                && FileUtils.exists(StringUtils.fixFolder(textField2.getText()) + Values.DEFAULT_STORAGE_FILE_NAME);
    }

    private void onCancel() {
// add your code here if necessary

        dispose();
        Window auth = FrameUtils.getWindows(AuthorizeDialog.class).get(0);
        if (auth != null) {
            auth.setVisible(true);
        }
        ArrayList<Window> mainForms = FrameUtils.getWindows(MainForm.class);
        if (mainForms != null) {
            for (Window w : mainForms) {
                w.setVisible(true);
            }
        }

        ArrayList<Window> settingsDialogs = FrameUtils.getWindows(SettingsDialog.class);
        if (settingsDialogs != null) {
            for (Window w : settingsDialogs) {
                w.setVisible(true);
            }
        }
    }

    public void saveOptions() {
        System.out.println("saving");
        String hexedPassword = Protector.hexPassword(Protector.hexPassword(new String(passwordField1.getPassword())));
        Protector.PASSWORD = hexedPassword.toCharArray();

        String key = StringUtils.fixFolder(textField1.getText());
        String storage = StringUtils.fixFolder(textField2.getText());
        PropertiesManager.changeProperties(key, storage);

        //FrameUtils.getWindow(""); //FIXME
        if (PasswordStorrager.framesMainForm.size() > 0) {
            if (PasswordStorrager.framesMainForm.get(PasswordStorrager.framesMainForm.size() - 1) != null) {
                PasswordStorrager.framesMainForm.set((PasswordStorrager.framesMainForm.size() - 1), null);
                new MainForm(new XmlParser().parseRecords());
            } else {
                new MainForm(new XmlParser().parseRecords());
            }
        } else {
            new MainForm(new XmlParser().parseRecords());
        }
        if (PasswordStorrager.framesAuthForm.size() > 0) {
            if (PasswordStorrager.framesAuthForm.get(PasswordStorrager.framesAuthForm.size() - 1) != null) {
                PasswordStorrager.framesAuthForm.get(PasswordStorrager.framesAuthForm.size() - 1).dispose();
                PasswordStorrager.framesAuthForm.set((PasswordStorrager.framesAuthForm.size() - 1), null);
            }
        }

    }
}
