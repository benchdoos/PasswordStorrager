package edu.passwordStorrager.utils.history;

import edu.passwordStorrager.gui.MainForm;
import edu.passwordStorrager.objects.Record;

import java.util.ArrayList;
import java.util.LinkedList;

public class History {
    private LinkedList<Object> windowHistory = new LinkedList<Object>();
    MainForm mainForm;
    ArrayList<Record> records = new ArrayList<>();

    private int current = -1;
    private int saved = -1;
    private boolean isHistoryCall = false;

    public History(MainForm mf) {
        mainForm = mf;
        records = mf.recordArrayList;
        System.out.println("><>" + mainForm.recordArrayList.equals(records));
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
            refreshTimer();
            System.out.println("History.registered: " + obj);
        }
    }

    private void refreshTimer() {
        MainForm.refreshLockTimer();
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
            refreshTimer();
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
            refreshTimer();
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

    public boolean equalRecordList(ArrayList<Record> one, ArrayList<Record> two) {
        if (one == null || two == null) {
            return false;
        }
        if (one.size() != two.size()) {
            return false;
        }
        for (int i = 0; i < one.size(); i++) {
            if (!one.get(i).getSite().equals(two.get(i).getSite())) {
                return false;
            }
            if (!one.get(i).getLogin().equals(two.get(i).getLogin())) {
                return false;
            }
            if (!one.get(i).getPassword().equals(two.get(i).getPassword())) {
                return false;
            }
        }
        return true;
    }
}
