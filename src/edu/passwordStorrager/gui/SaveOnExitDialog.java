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

        updateLocation(window, size);

        if (IS_MAC) {
            setUndecorated(true);
            new MovingTogether(window, this);
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

    private void updateLocation(MainForm window, Dimension size) {
        Point location = window.getLocation();
        Dimension dim = window.getSize();
        int centerWidth = location.x + dim.width / 2;
        centerWidth = centerWidth - size.width / 2;
        int height = location.y + window.getInsets().top + window.controlPanel.getHeight() + 1;
        setLocation(centerWidth, height);
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
        Core.setIsExitCanceled(true);
        dispose();
    }

    @Override
    public void setVisible(boolean value) {
        updateLocation(window,getSize());
        /*requestFocusInWindow();
        getRootPane().setDefaultButton(buttonSave);
        buttonDiscard.requestFocus();*/
        super.setVisible(value);
    }

    class MovingTogether extends ComponentAdapter {
        private MainForm window;
        private Window dialog;

        public MovingTogether(MainForm window, JDialog dialog) {
            this.window = window;
            this.dialog = dialog;
            if (window.getComponentListeners().length > 1) {
                window.removeComponentListener(this);
            }
            window.addComponentListener(this);
        }

        public void componentMoved(ComponentEvent e) {
            MainForm win = (MainForm) e.getComponent();
            Dimension size = dialog.getSize();
            if (win == window && dialog.isVisible()) {
                Point location = window.getLocation();
                Dimension dim = window.getSize();
                int centerWidth = location.x + dim.width / 2;
                centerWidth = centerWidth - size.width / 2;
                int height = location.y + window.getInsets().top + window.controlPanel.getHeight() + 1;
                dialog.setLocation(centerWidth, height);

            }
        }

    }
}
