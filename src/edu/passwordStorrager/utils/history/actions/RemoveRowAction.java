package edu.passwordStorrager.utils.history.actions;

import edu.passwordStorrager.gui.MainForm;
import edu.passwordStorrager.objects.Record;
import edu.passwordStorrager.utils.history.HistoryAction;

import java.util.ArrayList;

public class RemoveRowAction implements HistoryAction {
    int index = -1;
    ArrayList<Record> records;


    public RemoveRowAction(int index, ArrayList<Record> records) {
        this.index = index;
        this.records = records;
        System.out.println(records);
    }

    @Override
    public void undo(MainForm mainForm) {
        mainForm.addNewRecord(index, records.size());
        for (int i = 0; i < records.size(); i++) {
            mainForm.setValue(index + i, records.get(i));
        }
    }

    @Override
    public void redo(MainForm mainForm) {
        mainForm.deleteSelectedRecords(index, index + records.size()-1);
    }

    @Override
    public String toString() {
        return "RemoveRowAction: { index: [" + index + "] count: [" + records.size() + "]}";
    }
}
