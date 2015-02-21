package edu.passwordStorrager.gui;

import edu.passwordStorrager.core.Core;
import edu.passwordStorrager.utils.FrameUtils;
import edu.passwordStorrager.utils.platform.MacOsXUtils;
import edu.passwordStorrager.utils.platform.PlatformUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import static edu.passwordStorrager.core.Application.IS_MAC;

public class SaveOnExitDialog extends JDialog {
    private JPanel contentPane;
    private JLabel icon;
    private JButton buttonSave;
    private JButton buttonCancel;
    private JButton buttonDiscard;
    MainForm window;
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

        Point location = window.getLocation();
        Dimension dim = window.getSize();
        int centerWidth = location.x + dim.width / 2;
        centerWidth = centerWidth - size.width / 2;
        int height = location.y + 22;
        setLocation(centerWidth, height);

        if (IS_MAC) {
            setUndecorated(true);
        }

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
        window.disposeFrame();
        Core.setIsExitCanceled(false);
        dispose();
//        Core.onQuit(); //TODO remove when there will be more then one window
    }

    private void onSave() {
        window.saveStorage();
        window.dispose();
        Core.setIsExitCanceled(false);
        dispose();
//        Core.onQuit(); //TODO remove when there will be more then one window
    }

    private void onCancel() {
        if (MacOsXUtils.applicationQuitResponse != null) {
            MacOsXUtils.applicationQuitResponse.cancelQuit();
        }
        
        window.windowDisposeStatus = -1;

        Core.setIsExitCanceled(true);
        dispose();
    }
}
