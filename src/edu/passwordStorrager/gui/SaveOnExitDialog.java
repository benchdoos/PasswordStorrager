package edu.passwordStorrager.gui;

import edu.passwordStorrager.core.Core;
import edu.passwordStorrager.gui.elements.SimpleDialogSheet;
import edu.passwordStorrager.utils.FrameUtils;
import edu.passwordStorrager.utils.platform.MacOsXUtils;
import edu.passwordStorrager.utils.platform.PlatformUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class SaveOnExitDialog extends SimpleDialogSheet {
    private JPanel contentPane;
    private JLabel icon;
    private JButton buttonSave;
    private JButton buttonCancel;
    private JButton buttonDiscard;
//    MainForm window = (MainForm) MainForm.getFrames()[1];

    public SaveOnExitDialog(MainForm window) {
        this.window = window;
        Dimension size = new Dimension(570, 130);
        contentPane.setMinimumSize(size);
        contentPane.setPreferredSize(size);
        contentPane.setMaximumSize(size);

        setResizable(false);
        setContentPane(contentPane);
        setModal(true);

        getRootPane().setDefaultButton(buttonSave);
        if (!MacOsXUtils.isBundled()) {
            setIconImage(PlatformUtils.appIcon);
        }
        icon.setSize(new Dimension(80, 80));
        icon.setIcon(FrameUtils.resizeIcon(PlatformUtils.appIcon, icon.getSize()));

        initLocation(window, size);

        buttonDiscard.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onDiscard();
            }
        });

        buttonSave.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onSave();
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

        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        pack();
        buttonDiscard.requestFocus();
    }


    private void onDiscard() {
        setVisible(false);
        window.disposeFrame();
        Core.setIsExitCanceled(false);
        dispose();
    }

    private void onSave() {
        setVisible(false);
        window.saveStorage();
        window.dispose();
        Core.setIsExitCanceled(false);
        dispose();
    }

    private void onCancel() {
        setVisible(false);
        if (MacOsXUtils.applicationQuitResponse != null) {
            MacOsXUtils.applicationQuitResponse.cancelQuit();
        }
        Core.setIsExitCanceled(true);
        dispose();
    }
}
