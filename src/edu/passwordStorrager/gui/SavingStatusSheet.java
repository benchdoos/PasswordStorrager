package edu.passwordStorrager.gui;

import edu.passwordStorrager.gui.elements.SimpleDialogSheet;

import javax.swing.*;
import java.awt.*;

public class SavingStatusSheet extends SimpleDialogSheet {
    private JPanel contentPane;

    public JProgressBar progressBar;

    public SavingStatusSheet(MainForm mainForm) {
        this.window = mainForm;
//        Dimension size = new Dimension(570, 130);
        Dimension size = new Dimension(400, 100);
        setPreferredSize(size);

        setContentPane(contentPane);

        initLocation(window, size);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        pack();
    }

    @Override
    public void setVisible(boolean value) {
        setModal(value);
        progressBar.setIndeterminate(false);
        progressBar.setValue(0);
//        setModal(value);

        if (value) {
            MainForm.stopLockTimer();
        } else {
            MainForm.refreshLockTimer();
        }
        
        super.setVisible(value);
    }
}
