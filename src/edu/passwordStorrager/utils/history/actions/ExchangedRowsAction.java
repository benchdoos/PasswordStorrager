package edu.passwordStorrager.utils.history.actions;

import edu.passwordStorrager.gui.MainForm;
import edu.passwordStorrager.utils.history.HistoryAction;

public class ExchangedRowsAction implements HistoryAction {
    int row1 = -1;
    int row2 = -1;

    public ExchangedRowsAction(int row1, int row2) {
        this.row1 = row1;
        this.row2 = row2;
    }

    @Override
    public void undo(MainForm mainForm) {
        mainForm.exchangeRecords(row2,row1);
        mainForm.table.setRowSelectionInterval(row1,row1);
    }

    @Override
    public void redo(MainForm mainForm) {
        mainForm.exchangeRecords(row1, row2);
        mainForm.table.setRowSelectionInterval(row2, row2);
    }

    @Override
    public String toString() {
        return "ExchangeRowsAction: { row1: [" + row1 + "] row2: [" + row2 + "]}";    }
}
