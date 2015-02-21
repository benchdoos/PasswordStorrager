package edu.passwordStorrager.utils.history.actions;

import edu.passwordStorrager.gui.MainForm;
import edu.passwordStorrager.utils.history.HistoryAction;

import java.awt.*;

public class ChangeCellValueAction implements HistoryAction {
    private String prevValue;
    private Point cell;
    private String value;

    public ChangeCellValueAction(Point p, String prevValue, String newValue) {
        this.cell = p;
        this.prevValue = prevValue;
        this.value = newValue;
    }

    @Override
    public String toString() {
        return "ChangeCellValueAction: {cell[" + cell.x + ";" + cell.y + "] old value: [" + prevValue + "]; new value: [" + value + "]}";
    }

    @Override
    public void undo(MainForm mainForm) {
        mainForm.table.setValueAt(prevValue, cell.x, cell.y);
    }

    @Override
    public void redo(MainForm mainForm) {
        mainForm.table.setValueAt(value, cell.x, cell.y);
    }
}
