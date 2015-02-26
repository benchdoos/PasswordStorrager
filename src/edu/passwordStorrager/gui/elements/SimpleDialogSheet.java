package edu.passwordStorrager.gui.elements;

import edu.passwordStorrager.gui.MainForm;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import static edu.passwordStorrager.core.Application.IS_MAC;

public class SimpleDialogSheet extends JDialog {

    public MainForm window;
    
    public SimpleDialogSheet() {
        this.setResizable(false);
    }
    
    public void updateLocation(MainForm window, Dimension size) {
        Point location = window.getLocation();
        Dimension dim = window.getSize();
        int centerWidth = location.x + dim.width / 2;
        centerWidth = centerWidth - size.width / 2;
        int height = location.y + window.getInsets().top + window.controlPanel.getHeight() + 1;
        setLocation(centerWidth, height);
    }

    public void initLocation(MainForm window, Dimension size) {
        updateLocation(window, size);

        if (IS_MAC) {
            setUndecorated(true);
            new MovingTogether(window, this);
        }
    }
    
    @Override
    public void setVisible(boolean value) {
        updateLocation(window, getSize());
        /*requestFocusInWindow();
        getRootPane().setDefaultButton(buttonSave);
        buttonDiscard.requestFocus();*/
        super.setVisible(value);
    }

   public class MovingTogether extends ComponentAdapter {
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
