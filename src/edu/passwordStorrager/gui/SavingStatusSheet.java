package edu.passwordStorrager.gui;

import edu.passwordStorrager.gui.elements.SimpleDialogSheet;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class SavingStatusSheet extends SimpleDialogSheet {
    private JPanel contentPane;

    public JProgressBar progressBar;

    public SavingStatusSheet(MainForm mainForm) {
        this.window = mainForm;
//        Dimension size = new Dimension(570, 130);
        Dimension size = new Dimension(400, 100);
        setPreferredSize(size);
        
        setContentPane(contentPane);
        setModal(true);

        initLocation(window,size);
        
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });
             
        
        pack();
    }

    private void onCancel() {
// add your code here if necessary
        dispose();
    }
}
