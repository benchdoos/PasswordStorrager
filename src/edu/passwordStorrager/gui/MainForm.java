package edu.passwordStorrager.gui;

import edu.passwordStorrager.objects.Record;
import edu.passwordStorrager.utils.StringUtils;
import edu.passwordStorrager.xmlManager.XmlParser;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
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
    static Timer timer;

    static final String NUMBER_COLUMN_NAME = "#", SITE_COLUMN_NAME = "Сайт", LOGIN_COLUMN_NAME = "Логин", PASSWORD_COLUMN_NAME = "Пароль";


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
    private JScrollPane scrollPane;
    private JTable table;
    private JPanel statusPanel;

    public MainForm(ArrayList<Record> recordArrayList) {
        this.recordArrayList = recordArrayList;
        initComponents();
    }


    public void setStatus(String status, int type) {
        statusPanel.setPreferredSize(new Dimension(-1, 17));
        statusPanel.setMinimumSize(new Dimension(-1, 17));
        statusPanel.setMaximumSize(new Dimension(-1, 17));
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

        if (timer == null) {
            timer = new Timer(8 * 1000, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    resetStatus();
                }
            });
            timer.setRepeats(false);
            timer.start();
        } else {
            timer.restart();
        }

    }

    public void resetStatus() {
        bar.setForeground(Color.black);
        bar.setText("");
        statusPanel.setPreferredSize(new Dimension(-1, -1));
        statusPanel.setMinimumSize(new Dimension(-1, -1));
        statusPanel.setMaximumSize(new Dimension(-1, -1));
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

        loadList();

        pack();
    }

    private void initStatusBar() {
        statusPanel.setLayout(new BorderLayout());
        bar = new JLabel("", JLabel.LEFT);
        bar.setFont(new Font("Menlo", Font.PLAIN, 10));
        bar.setForeground(Color.black);
        bar.setVerticalAlignment(JLabel.TOP);
        statusPanel.add(bar);
    }

    private void initTableListeners() {
        MouseListener copyMouseListener = new MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                JTable table = (JTable) me.getSource();
                Point p = me.getPoint();
                int row = table.rowAtPoint(p);
                if (me.getClickCount() == 2 && me.getButton() == MouseEvent.BUTTON1) {
                    if (MainForm.this.table.getSelectedRow() >= 0) {
                        if (MainForm.this.table.getSelectedColumn() == table.getColumn(SITE_COLUMN_NAME).getModelIndex()) {
                            java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
                            try {
                                desktop.browse(URI.create(StringUtils.parseUrl((String) MainForm.this.table.getModel().getValueAt(row, table.getColumn(SITE_COLUMN_NAME).getModelIndex()))));
                            } catch (IOException e) {
                                System.out.println("Can not open in browser: " + StringUtils.parseUrl((String) MainForm.this.table.getModel().getValueAt(row, 0)));
                            }
                        } else {
                            copyToClipboard((String) MainForm.this.table.getModel().getValueAt(row, table.getColumn(PASSWORD_COLUMN_NAME).getModelIndex()));
                        }
                    }
                }
            }
        };
        table.addMouseListener(copyMouseListener);

        table.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (editModeJRadioButtonMenuItem.isSelected()) {
                    if ((e.getKeyCode() == KeyEvent.VK_C) && ((e.getModifiers() & KeyEvent.META_DOWN_MASK) != 0)) {
                        /*if (table.getSelectedRow() != -1) {
                            copyToClipboard((String) table.getModel().getValueAt(table.getSelectedRow(), 2));
                        }*/
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });
        /*scrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
            public void adjustmentValueChanged(AdjustmentEvent e) {
                e.getAdjustable().setValue(e.getAdjustable().getMaximum());
            }
        });*/
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
                loadList();
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
                new SettingsDialog(){

                    @Override
                    public void onOK() {
                        this.saveSettings();
                        setStatus("Настройки сохранены", STATUS_SUCCESS);
                        this.dispose();
                    }
                };
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
                    table.getCellEditor().cancelCellEditing();
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
                deleteSelectedRecord();
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

                copyToClipboard((String) table.getModel().getValueAt(table.getSelectedRow(), 0));

            }
        });
        menuItemCopyLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (table.getSelectedRow() != -1) {
                    copyToClipboard((String) table.getModel().getValueAt(table.getSelectedRow(), 1));
                }
            }
        });

    }


    private void resizeTableColumns() {
        /*for (int column = 0; column < table.getColumnCount(); column++) {
            TableColumn tableColumn = table.getColumnModel().getColumn(column);
            int preferredWidth = tableColumn.getMinWidth();
            int maxWidth = tableColumn.getMaxWidth();

            for (int row = 0; row < table.getRowCount(); row++) {
                TableCellRenderer cellRenderer = table.getCellRenderer(row, column);
                Component c = table.prepareRenderer(cellRenderer, row, column);
                int width = c.getPreferredSize().width + table.getIntercellSpacing().width;
                preferredWidth = Math.max(preferredWidth, width);

                //  We've exceeded the maximum width, no need to check other rows

                if (preferredWidth >= maxWidth) {
                    preferredWidth = maxWidth;
                    break;
                }
            }

            tableColumn.setPreferredWidth(preferredWidth);
        }*/
        TableColumn tableColumn = table.getColumn(NUMBER_COLUMN_NAME);
        int column = table.getColumn(NUMBER_COLUMN_NAME).getModelIndex();
        int preferredWidth = tableColumn.getMinWidth();
        int maxWidth = tableColumn.getMaxWidth();
        for (int row = 0; row < table.getRowCount(); row++) {
            TableCellRenderer cellRenderer = table.getCellRenderer(row, column);
            Component c = table.prepareRenderer(cellRenderer, row, column);
            int width = c.getPreferredSize().width + table.getIntercellSpacing().width;
            preferredWidth = Math.max(preferredWidth, width);

            //  We've exceeded the maximum width, no need to check other rows

            if (preferredWidth >= maxWidth) {
                preferredWidth = maxWidth;
                break;
            }
        }
        tableColumn.setPreferredWidth(preferredWidth);
    }


    private void copySelectedCell(int column) {
        if (table.getSelectedRow() >= 0) {
            System.out.println("{{{" + table.getColumn("Сайт").getModelIndex());
            copyToClipboard((String) table.getModel().getValueAt(table.getSelectedRow(), column));
        }
    }

    private void loadList() {
        //Record[] recordsList = recordArrayList.toArray(new Record[recordArrayList.size()]);
        table.setModel(createTableModel(recordArrayList));
        table.setRowHeight(20);

        TableColumn number = table.getColumnModel().getColumn(0);
        number.setHeaderValue(NUMBER_COLUMN_NAME);
        number.setMinWidth(20);
        //number.setWidth(20);
        number.setMaxWidth(40);
        number.setPreferredWidth(number.getPreferredWidth());
        number.sizeWidthToFit();

        //number.setResizable(false);

        TableColumn site = table.getColumnModel().getColumn(1);
        site.setHeaderValue(SITE_COLUMN_NAME);
        site.setWidth(150);
        site.setResizable(false);

        TableColumn login = table.getColumnModel().getColumn(2);
        login.setHeaderValue(LOGIN_COLUMN_NAME);
        login.setWidth(150);
        login.setResizable(false);

        TableColumn password = table.getColumnModel().getColumn(3);
        password.setHeaderValue(PASSWORD_COLUMN_NAME);
        password.setResizable(false);

        table.getColumn(NUMBER_COLUMN_NAME).setCellEditor(new TableEditor(new JTextField(NUMBER_COLUMN_NAME)));
        table.getColumn(SITE_COLUMN_NAME).setCellEditor(new TableEditor(new JTextField("field")));
        table.getColumn(LOGIN_COLUMN_NAME).setCellEditor(new TableEditor(new JTextField("field")));
        table.getColumn(PASSWORD_COLUMN_NAME).setCellEditor(new TableEditor(new JTextField("field")));

        resizeTableColumns();

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setDragEnabled(false);
        table.getTableHeader().setReorderingAllowed(false); //to prevent column dragging(moving)

        table.setComponentPopupMenu(popupMenu);
        table.setSurrendersFocusOnKeystroke(true);

        setStatus("Количество записей: " + table.getModel().getRowCount(), STATUS_MESSAGE);
    }

    private void saveStorage() {
        int rows = table.getRowCount();
        System.out.println("rows to save:" + rows);
        recordArrayList = new ArrayList<Record>(rows);
        for (int i = 0; i < rows; i++) {
            Record record = new Record();
            record.setSite((String) table.getModel().getValueAt(i, table.getColumn(SITE_COLUMN_NAME).getModelIndex()));
            record.setLogin((String) table.getModel().getValueAt(i, table.getColumn(LOGIN_COLUMN_NAME).getModelIndex()));
            record.setPassword((String) table.getModel().getValueAt(i, table.getColumn(PASSWORD_COLUMN_NAME).getModelIndex()));
            recordArrayList.add(record);
        }
        editModeJRadioButtonMenuItem.setSelected(false);
        new XmlParser().saveRecords(recordArrayList);
        loadList();
        setStatus("Сохранено", STATUS_SUCCESS);
    }

    private void addNewRecord() {
        recordArrayList.add(new Record());
        loadList();
        editModeJRadioButtonMenuItem.setSelected(true);
        table.clearSelection();
        table.setRowSelectionInterval(table.getModel().getRowCount() - 1, table.getModel().getRowCount() - 1);
        scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
    }

    private void deleteSelectedRecord() {
        int index = table.getSelectedRow();
        if (index >= 0) {
            recordArrayList.remove(index);
            loadList();
            table.clearSelection();

            if (index >= 0 && recordArrayList.size() > 0) {
                if (index < recordArrayList.size()) {
                    table.setRowSelectionInterval(index, index);
                } else {
                    table.setRowSelectionInterval(recordArrayList.size() - 1, recordArrayList.size() - 1);
                }
            }
        }
    }

    private void copyToClipboard(String value) {
        System.out.println("copy [" + value + "]");
        StringSelection stringSelection = new StringSelection(value);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }
    
    
    private DefaultTableModel createTableModel(ArrayList<Record> recordArrayList) {
        DefaultTableModel tableModel = new DefaultTableModel();
        String[] number = new String[recordArrayList.size()];
        String[] siteData = new String[recordArrayList.size()];
        String[] loginData = new String[recordArrayList.size()];
        String[] pwdData = new String[recordArrayList.size()];

        for (int i = 0; i < recordArrayList.size(); i++) {
            number[i] = (i + 1) + "";
            siteData[i] = recordArrayList.get(i).getSite();
            loginData[i] = recordArrayList.get(i).getLogin();
            pwdData[i] = recordArrayList.get(i).getPassword();
        }

        tableModel.addColumn(NUMBER_COLUMN_NAME, number);
        tableModel.addColumn(SITE_COLUMN_NAME, siteData);
        tableModel.addColumn(LOGIN_COLUMN_NAME, loginData);
        tableModel.addColumn(PASSWORD_COLUMN_NAME, pwdData);
        return tableModel;
    }

    static class TableEditor extends DefaultCellEditor {

        JTextField textField;

        public TableEditor(JTextField textField) {
            super(textField);
            this.textField = textField;
        }

        @Override
        public boolean isCellEditable(EventObject anEvent) {
            return textField != null && !textField.getText().equals("#") && editModeJRadioButtonMenuItem.isSelected();
        }
    }

}
