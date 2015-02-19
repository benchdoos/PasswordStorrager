package edu.passwordStorrager.utils.history;

import edu.passwordStorrager.gui.MainForm;

public class AddRowAction implements HistoryAction {
    int index = -1;
    int count = -1;

    public AddRowAction(int index, int count) {
        this.index = index;
        this.count = count;
    }

    @Override
    public void undo(MainForm mainForm) {
        mainForm.deleteSelectedRecords(index, index + count-1);
    }

    @Override
    public void redo(MainForm mainForm) {
        mainForm.addNewRecord(index, count);
    }

    @Override
    public String toString() {
        return "AddRowAction: { index: [" + index + "] count: [" + count + "]}";
    }
}
