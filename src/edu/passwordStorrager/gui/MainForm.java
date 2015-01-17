package edu.passwordStorrager.gui;

import edu.passwordStorrager.objects.Record;
import edu.passwordStorrager.xmlManager.XmlParser;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.EventObject;

public class MainForm extends JFrame {
    public static final int STATUS_MESSAGE = 1, STATUS_ERROR = -1, STATUS_SUCCESS = 2;
    protected static JRadioButtonMenuItem editModeJRadioButtonMenuItem; //if checked - can edit existing
    protected static JLabel bar;
    private ArrayList<Record> recordArrayList = new ArrayList<>();
    private JPopupMenu popupMenu;
    private JMenuBar jMenuBar1;
    private JMenu fileJMenu;
    private JMenuItem openItem;
    private JMenuItem saveItem;
    private JMenuItem settingsItem;
    private JMenu editJMenu;
    private JMenuItem addItem;
    private JMenuItem deleteItem;
    private JMenu copyJMenu;
    private JMenuItem copySiteItem;
    private JMenuItem copyLoginItem;
    private JMenuItem copyPasswordItem;
    private JPanel panel1;
    private JScrollPane jScrollPane1;
    private JTable table1;
    private JPanel statusPanel;
    private DefaultTableModel tableModel;

    public MainForm(ArrayList<Record> recordArrayList) {
        /*try {
            if (recordArrayList != null) {
                if (recordArrayList.size() > 0) {
                    this.recordArrayList = recordArrayList;
                    initComponents();
                } else {
                    throw new RuntimeException();
                }
            } else {
                throw new RuntimeException();
            }
        } catch (RuntimeException e) {
            //TODO throw notif here
            System.err.println("Storage file is empty");
        }*/

        this.recordArrayList = recordArrayList;
        initComponents();
    }

    public static void setStatus(String status, int type) {
        switch (type) {
            case STATUS_MESSAGE:
                bar.setForeground(Color.black);
                bar.setText(status);
                break;
            case STATUS_SUCCESS:
                bar.setForeground(new Color(0, 150, 0));
                bar.setText(status);
                break;
            case STATUS_ERROR:
                bar.setForeground(Color.red);
                bar.setText(status);
                break;
        }

        Timer timer = new Timer(8 * 1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetStatus();
            }
        });
        timer.setRepeats(false);
        timer.start();
    }

    public static void resetStatus() {
        bar.setForeground(Color.black);
        bar.setText("");
    }

    private void initComponents() {
        setContentPane(panel1);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(600, 430));
        setPreferredSize(new java.awt.Dimension(600, 430));
        //setResizable(false);
        setSize(new java.awt.Dimension(600, 430));

        initTableListeners();

        initStatusBar();

        initMenu();

        initPopUp();

        initList();

        pack();
    }

    private void initStatusBar() {
        statusPanel.setLayout(new BorderLayout());
        bar = new JLabel("~~~", JLabel.LEFT);
        bar.setFont(new Font("Lucida Grande", Font.BOLD, 10));
        bar.setForeground(Color.black);
        statusPanel.add(bar);
    }

    private void initTableListeners() {
        MouseListener copyMouseListener = new MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                JTable table = (JTable) me.getSource();
                Point p = me.getPoint();
                int row = table.rowAtPoint(p);
                if (me.getClickCount() == 2) {

                    if (table1.getSelectedRow() >= 0) {
                        if (table1.getSelectedColumn() == 1 || table1.getSelectedColumn() == 2) {
                            copyToClipboard((String) table1.getModel().getValueAt(row, 2));
                        } else {
                            java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
                            try {
                                desktop.browse(URI.create(parseUrl((String) table1.getModel().getValueAt(row, 0))));
                            } catch (IOException e) {
                                System.out.println("Can not open in browser: " + parseUrl((String) table1.getModel().getValueAt(row, 0)));
                            }
                        }
                    }

                }
            }
        };
        table1.addMouseListener(copyMouseListener);

        table1.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (editModeJRadioButtonMenuItem.isSelected()) {
                    if ((e.getKeyCode() == KeyEvent.VK_C) && ((e.getModifiers() & KeyEvent.META_DOWN_MASK) != 0)) {
                        if (table1.getSelectedRow() != -1) {
                            copyToClipboard((String) table1.getModel().getValueAt(table1.getSelectedRow(), 2));
                        }
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });
    }

    private void initMenu() {
        jMenuBar1 = new JMenuBar();

        fileJMenu = new JMenu();
        openItem = new JMenuItem();
        saveItem = new JMenuItem();
        settingsItem = new JMenuItem();

        editJMenu = new JMenu();
        editModeJRadioButtonMenuItem = new JRadioButtonMenuItem();
        addItem = new JMenuItem();
        deleteItem = new JMenuItem();

        copyJMenu = new JMenu();
        copySiteItem = new JMenuItem();
        copyLoginItem = new JMenuItem();
        copyPasswordItem = new JMenuItem();

        fileJMenu.setText("Файл");

        openItem.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.META_MASK));
        openItem.setText("Открыть");
        openItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                recordArrayList = new XmlParser().parseRecords();
                initList();
            }
        });
        fileJMenu.add(openItem);

        saveItem.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.META_MASK));
        saveItem.setText("Сохранить");
        saveItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveStorage();
            }
        });
        fileJMenu.add(saveItem);

        jMenuBar1.add(fileJMenu);

        settingsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, InputEvent.META_MASK));
        settingsItem.setText("Настройки");
        settingsItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new SettingsDialog();
            }
        });
        fileJMenu.add(settingsItem);

        editJMenu.setText("Правка");

        editModeJRadioButtonMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.META_MASK));
        editModeJRadioButtonMenuItem.setSelected(false);
        editModeJRadioButtonMenuItem.setText("Режим редактирования");
        editModeJRadioButtonMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!editModeJRadioButtonMenuItem.isSelected()) {
                    table1.getCellEditor().cancelCellEditing();
                }
            }
        });

        editJMenu.add(editModeJRadioButtonMenuItem);

        addItem.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.META_MASK));
        addItem.setText("Добавить");
        addItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addNewRecord();
            }
        });
        editJMenu.add(addItem);

        deleteItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, InputEvent.META_MASK));
        deleteItem.setText("Удалить");
        deleteItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteSelectedRow();
            }
        });
        editJMenu.add(deleteItem);

        jMenuBar1.add(editJMenu);

        copyJMenu.setText("Копировать");

        copySiteItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, InputEvent.META_MASK));
        copySiteItem.setText("Копировать сайт");
        copySiteItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copySelectedCell(0);
            }
        });
        copyJMenu.add(copySiteItem);

        copyLoginItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, InputEvent.META_MASK));
        copyLoginItem.setText("Копировать логин");
        copyLoginItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copySelectedCell(1);
            }
        });
        copyJMenu.add(copyLoginItem);

        copyPasswordItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, InputEvent.META_MASK));
        copyPasswordItem.setText("Копировать пароль");
        copyPasswordItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copySelectedCell(2);
            }
        });
        copyJMenu.add(copyPasswordItem);

        jMenuBar1.add(copyJMenu);

        setJMenuBar(jMenuBar1);
    }

    private void copySelectedCell(int column) {
        if (table1.getSelectedRow() >= 0) {
            copyToClipboard((String) table1.getModel().getValueAt(table1.getSelectedRow(), column));
        }
    }

    private void addNewRecord() {
        recordArrayList.add(new Record());
        initList();
        editModeJRadioButtonMenuItem.setSelected(true);
        table1.clearSelection();
        table1.setRowSelectionInterval(tableModel.getRowCount() - 1, tableModel.getRowCount() - 1);
    }

    private void deleteSelectedRow() {
        int index = table1.getSelectedRow();
        if (index >= 0) {
            recordArrayList.remove(index);
            initList();
            table1.clearSelection();

            if (index >= 0 && recordArrayList.size() > 0) {
                if (index < recordArrayList.size()) {
                    table1.setRowSelectionInterval(index, index);
                } else {
                    table1.setRowSelectionInterval(recordArrayList.size() - 1, recordArrayList.size() - 1);
                }
            }
        }
    }

    private void saveStorage() {
        int rows = table1.getRowCount();
        System.out.println("rows to save:" + rows);
        recordArrayList = new ArrayList<Record>(rows);
        for (int i = 0; i < rows; i++) {
            Record record = new Record();
            record.setSite((String) table1.getModel().getValueAt(i, 0));
            record.setLogin((String) table1.getModel().getValueAt(i, 1));
            record.setPassword((String) table1.getModel().getValueAt(i, 2));
            recordArrayList.add(record);
        }
        editModeJRadioButtonMenuItem.setSelected(false);
        new XmlParser().saveRecords(recordArrayList);
        initList();
        setStatus("Сохранено", STATUS_SUCCESS);
    }

    private void initPopUp() {
        popupMenu = new JPopupMenu();
        JMenuItem menuItemAdd = new JMenuItem("Добавить");
        JMenuItem menuItemCopySite = new JMenuItem("Копировать сайт");

        JMenuItem menuItemCopyLogin = new JMenuItem("Копировать логин");

        popupMenu.add(menuItemAdd);
        popupMenu.add(menuItemCopySite);
        popupMenu.add(menuItemCopyLogin);

        menuItemCopySite.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                copyToClipboard((String) table1.getModel().getValueAt(table1.getSelectedRow(), 0));

            }
        });
        menuItemCopyLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (table1.getSelectedRow() != -1) {
                    copyToClipboard((String) table1.getModel().getValueAt(table1.getSelectedRow(), 1));
                }
            }
        });

    }

    private void initList() {
        //Record[] recordsList = recordArrayList.toArray(new Record[recordArrayList.size()]);
        tableModel = createTableModel(recordArrayList);
        table1.setModel(tableModel);
        table1.setRowHeight(20);

        TableColumn site = table1.getColumnModel().getColumn(0);
        site.setHeaderValue("Сайт");
        site.setWidth(150);
        site.setResizable(false);

        TableColumn login = table1.getColumnModel().getColumn(1);
        login.setHeaderValue("Логин");
        login.setWidth(150);
        login.setResizable(false);

        TableColumn password = table1.getColumnModel().getColumn(2);
        password.setHeaderValue("Пароль");
        password.setResizable(false);

        table1.getColumn("Сайт").setCellEditor(new TableEditor(new JTextField("field")));
        table1.getColumn("Логин").setCellEditor(new TableEditor(new JTextField("field")));
        table1.getColumn("Пароль").setCellEditor(new TableEditor(new JTextField("field")));

        table1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table1.setDragEnabled(false);
        table1.getTableHeader().setReorderingAllowed(false); //to prevent column dragging(moving)

        table1.setComponentPopupMenu(popupMenu);

        setStatus("Количество записей: " + table1.getModel().getRowCount(), STATUS_MESSAGE);
    }

    private String parseUrl(String value) {
        if (value.length() > 7) {
            if (!value.contains("http://")) {
                value = "http://" + value;
            }
        } else {
            value = "http://" + value;
        }
        return value;
    }

    private void copyToClipboard(String value) {
        System.out.println("copy [" + value + "]");
        StringSelection stringSelection = new StringSelection(value);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }

    private DefaultTableModel createTableModel(ArrayList<Record> recordArrayList) {
        String[] siteData = new String[recordArrayList.size()];
        String[] loginData = new String[recordArrayList.size()];
        String[] pwdData = new String[recordArrayList.size()];

        for (int i = 0; i < recordArrayList.size(); i++) {
            siteData[i] = recordArrayList.get(i).getSite();
            loginData[i] = recordArrayList.get(i).getLogin();
            pwdData[i] = recordArrayList.get(i).getPassword();
        }

        tableModel = new DefaultTableModel();
        tableModel.addColumn("Сайт", siteData);
        tableModel.addColumn("Логин", loginData);
        tableModel.addColumn("Пароль", pwdData);
        return tableModel;
    }

    static class TableEditor extends DefaultCellEditor {

        public TableEditor(JTextField textField) {
            super(textField);
        }

        @Override
        public boolean isCellEditable(EventObject anEvent) {
            return editModeJRadioButtonMenuItem.isSelected();
        }
    }
}
