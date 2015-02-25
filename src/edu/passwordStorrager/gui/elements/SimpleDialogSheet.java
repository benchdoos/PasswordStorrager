package edu.passwordStorrager.gui.elements;

import edu.passwordStorrager.gui.MainForm;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class SimpleDialogSheet extends JDialog {

    public MainForm window;
    
    public void updateLocation(MainForm window, Dimension size) {
        Point location = window.getLocation();
        Dimension dim = window.getSize();
        int centerWidth = location.x + dim.width / 2;
        centerWidth = centerWidth - size.width / 2;
        int height = location.y + window.getInsets().top + window.controlPanel.getHeight() + 1;
        setLocation(centerWidth, height);
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
