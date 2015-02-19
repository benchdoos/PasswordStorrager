package edu.passwordStorrager.utils.history;

import edu.passwordStorrager.gui.MainForm;

public interface HistoryAction {
    void undo(MainForm mainForm);
    void redo(MainForm mainForm);
}
