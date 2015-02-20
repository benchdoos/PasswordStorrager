package edu.passwordStorrager.utils.history;

import edu.passwordStorrager.gui.MainForm;

import java.util.LinkedList;

public class History {
    private LinkedList<Object> windowHistory = new LinkedList<Object>();
    private int current = -1;
    MainForm mainForm;
    private int saved = -1;
    private boolean isHistoryCall = false;

    public History(MainForm mf) {
        mainForm = mf;
        mainForm.undoItem.setEnabled(false);
        mainForm.redoItem.setEnabled(false);
    }

    public void register(Object obj) {
        if (implementsInterface(obj)) {
            if (current == windowHistory.size() - 1) {
                windowHistory.add(obj);
                current = windowHistory.size() - 1;
            } else {
                while (current != windowHistory.size() - 1) {//FIXME
                    windowHistory.removeLast();
                }
                windowHistory.add(obj);
                current = windowHistory.size() - 1;
            }
            updateIfIsSaved();
            System.out.println("History.registered: " + obj);
        }
    }

    public void undo() {
        if (current >= 0) {
            HistoryAction h = (HistoryAction) windowHistory.get(current);
            isHistoryCall = true;
            h.undo(mainForm);
            isHistoryCall = false;
            current--;
            updateIfIsSaved();
            System.out.println("History.undo: " + h);
        }
    }

    public void redo() {
        if (current < windowHistory.size() - 1) {
            current++;
            HistoryAction h = (HistoryAction) windowHistory.get(current);
            isHistoryCall = true;
            h.redo(mainForm);
            isHistoryCall = false;
            updateIfIsSaved();
            System.out.println("History.redo: " + h);
        }
    }

    public boolean isHistoryCall() {
        return isHistoryCall;
    }

    public void save() {
        saved = current;
        mainForm.setEdited(false);
    }

    private void updateIfIsSaved() {
        if (saved != current) {
            mainForm.setEdited(true);
        } else {
            mainForm.setEdited(false);
        }
        if (windowHistory.size() > 0) {
            if (current == -1) {
                mainForm.undoItem.setEnabled(false);
            } else {
                mainForm.undoItem.setEnabled(true);
            }
            if (current == windowHistory.size() - 1) {
                mainForm.redoItem.setEnabled(false);
            } else {
                mainForm.redoItem.setEnabled(true);
            }
        }
    }

    private static boolean implementsInterface(Object object) {
        return HistoryAction.class.isInstance(object);
    }
}
