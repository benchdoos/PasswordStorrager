package edu.passwordStorrager.gui;

import javax.swing.*;
import java.awt.event.*;

public abstract class AccountEnterDialog extends JDialog {
    JPasswordField passwordField1;
    JTextField textField1;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;

    public AccountEnterDialog(String windowName, String login, String password) {
        setContentPane(contentPane);
        setTitle(windowName);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        textField1.setText(login);
        passwordField1.setText(password);

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

        pack();
        setVisible(true);
    }

    abstract void onOK();

    private void onCancel() {
// add your code here if necessary
        dispose();
    }
}
