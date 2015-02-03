package edu.passwordStorrager.gui;

import edu.passwordStorrager.core.Main;
import edu.passwordStorrager.core.PropertiesManager;
import edu.passwordStorrager.objects.Record;
import edu.passwordStorrager.protector.Values;
import edu.passwordStorrager.utils.FrameUtils;
import edu.passwordStorrager.utils.StringUtils;
import edu.passwordStorrager.xmlManager.XmlParser;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.EventObject;

import static edu.passwordStorrager.utils.FrameUtils.*;

public class MainForm extends JFrame {

    private static final Logger log = Logger.getLogger(getCurrentClassName());

    public static final int STATUS_MESSAGE = 1, STATUS_ERROR = -1, STATUS_SUCCESS = 2;
    protected static JRadioButtonMenuItem editModeJRadioButtonMenuItem; //if checked - can edit existing
    protected static JLabel bar;
    static Timer timer;

    static final String NUMBER_COLUMN_NAME = "#", SITE_COLUMN_NAME = "Сайт", LOGIN_COLUMN_NAME = "Логин", PASSWORD_COLUMN_NAME = "Пароль";

    private boolean isEdited = false;

    public ArrayList<Record> recordArrayList = new ArrayList<>();


    private JPopupMenu popupMenu;
    public static JMenuBar jMenuBar1;
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
    private JButton addDown;
    private JButton addUp;
    private JTextField searchField;
    private JLabel isEditableLable;
    private Timer searchTimer;

    public MainForm(ArrayList<Record> recordArrayList) {
        this.recordArrayList = recordArrayList;
        initComponents();
        setVisible(true);
        Main.framesMainForm.add(this);
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
        updateTitle(new File(Main.propertiesApplication.getProperty(PropertiesManager.KEY_NAME) + Values.DEFAULT_STORAGE_FILE_NAME));
        setContentPane(panel1);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        //setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(600, 430));
        setPreferredSize(getFrameSize(getCurrentClassName()));
        setLocation(getFrameLocation(getCurrentClassName()));

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (isEdited) {

                } else {
                    FrameUtils.setFrameLocation(getClass().getEnclosingClass().getName(), getLocation());
                    FrameUtils.setFrameSize(getClass().getEnclosingClass().getName(), getSize());
                    Main.onQuit();
                }
            }
        });

        initSearchBarListeners();

        initTableListeners();

        initControlBar();

        initStatusBar();

        initMenu();

        initPopUp();

        loadList(recordArrayList);

        //request focus on table

        pack();
        table.requestFocus();
        if (table.getRowCount() > 0) {
            table.setRowSelectionInterval(0, 0);
        }
    }

    private void initSearchBarListeners() {
        panel1.registerKeyboardAction(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        searchField.requestFocus();
                    }
                }, KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.META_MASK),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        searchField.putClientProperty("JTextField.variant", "search");

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            void search() {
                if (!searchField.getText().isEmpty()) {
                    initSearchTimer(searchField.getText());
                } else {
                    searchTimer.stop();
                    loadList(recordArrayList);
                }
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                search();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                search();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                search();
            }
        });

        searchField.addFocusListener(new FocusListener() {
            private void changeSearchFieldSize(int width) {
                int height = searchField.getHeight();
                searchField.setMinimumSize(new Dimension(width, height));
                searchField.setPreferredSize(new Dimension(width, height));
                searchField.setMaximumSize(new Dimension(width, height));
                searchField.setSize(new Dimension(width, height));
            }

            
            //TODO add resizing to searchField
            @Override
            public void focusGained(FocusEvent e) {
                int width = 300;
                //changeSearchFieldSize(width);

                System.out.println("gained");
            }

            @Override
            public void focusLost(FocusEvent e) {
                int width = 150;
//                changeSearchFieldSize(width);

                System.out.println("lost");
            }
        });

        ActionListener clearSearchFieldAction = new ActionListener() {
            void clear() {
                if (!searchField.getText().isEmpty()) {
                    searchField.setText("");
                    loadList(recordArrayList);
                } else {
                    table.requestFocus();
                    table.setRowSelectionInterval(0, 0);
                }
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                clear();
            }
        };

        searchField.registerKeyboardAction(clearSearchFieldAction, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_FOCUSED);
        searchField.registerKeyboardAction(clearSearchFieldAction, KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 
                InputEvent.META_DOWN_MASK), JComponent.WHEN_FOCUSED);
    }

    private void initSearchTimer(final String text) {
        if (searchTimer != null) {
            searchTimer.stop();
        }
        searchTimer = new Timer(500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadList(searchRecord(text));
            }
        });
        searchTimer.setRepeats(false);
        searchTimer.start();
    }

    private ArrayList<Record> searchRecord(String text) {
        if (text.isEmpty()) {
            return recordArrayList;
        }
        ArrayList<Record> foundRecords = new ArrayList<Record>(recordArrayList.size());
        for (Record record : recordArrayList) {
            try {
                if (record.getSite().contains(text) || record.getLogin().contains(text)) {
                    foundRecords.add(record);
                }
            } catch (NullPointerException ignored) {
            }
        }
        return foundRecords;
    }

    private void initControlBar() {
        addDown.putClientProperty("JButton.buttonType", "roundRect");
        addUp.putClientProperty("JButton.buttonType", "roundRect");
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
                                log.warn("Can not open in browser: " + StringUtils.parseUrl((String) MainForm.this.table.getModel().getValueAt(row, table.getColumn(SITE_COLUMN_NAME).getModelIndex())));
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
                loadList(recordArrayList);
                setEdited(false);
                isEditableLable.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage("NSImage://NSLockLockedTemplate")));
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
                new SettingsDialog() {

                    @Override
                    public void onOK() {
                        this.saveSettings();
                        setStatus("Настройки сохранены", STATUS_SUCCESS);
                        this.dispose();
                    }
                };
            }
        });

        //fileJMenu.add(settingsItem);

        editJMenu.setText("Правка");

        editModeJRadioButtonMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.META_MASK));
        editModeJRadioButtonMenuItem.setSelected(false);
        editModeJRadioButtonMenuItem.setText("Режим редактирования");
        editModeJRadioButtonMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!editModeJRadioButtonMenuItem.isSelected()) {
                    isEditableLable.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage("NSImage://NSLockUnlockedTemplate")));
                    int index = table.getSelectedRow();
                    try {
                        table.getCellEditor().cancelCellEditing();
                    } catch (NullPointerException ignored) {
                    }
//                    table.getCellEditor(table.getEditingRow(), table.getEditingColumn()).cancelCellEditing();
                    table.setRowSelectionInterval(index, index);
                    //table.clearSelection();
                } else {
                    isEditableLable.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage("NSImage://NSLockLockedTemplate")));
                }
            }
        });

        isEditableLable.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage("NSImage://NSLockLockedTemplate")));
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


    private void resizeTableColumns(TableColumn tableColumn) {
        int column = table.getColumn(NUMBER_COLUMN_NAME).getModelIndex();
        int preferredWidth = tableColumn.getMinWidth();
        int maxWidth = tableColumn.getMaxWidth();
        for (int row = 0; row < table.getRowCount(); row++) {
            TableCellRenderer cellRenderer = table.getCellRenderer(row, column);
            Component c = table.prepareRenderer(cellRenderer, row, column);
            int width = c.getPreferredSize().width + table.getIntercellSpacing().width;
            preferredWidth = Math.max(preferredWidth, width);

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

    public void loadList(ArrayList<Record> recordArrayList) {
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

        resizeTableColumns(table.getColumn(NUMBER_COLUMN_NAME));

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
        loadList(recordArrayList);
        setEdited(false);
        setStatus("Сохранено", STATUS_SUCCESS);
    }

    private void addNewRecord() {
        recordArrayList.add(new Record());
        loadList(recordArrayList);
        if (!editModeJRadioButtonMenuItem.isSelected()) {
            editModeJRadioButtonMenuItem.doClick();
        }
        isEditableLable.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage("NSImage://NSLockUnlockedTemplate")));
        table.clearSelection();
        table.setRowSelectionInterval(table.getModel().getRowCount() - 1, table.getModel().getRowCount() - 1);
        scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
        setEdited(true);
    }

    private void deleteSelectedRecord() {
        int index = table.getSelectedRow();
        if (index >= 0) {
            recordArrayList.remove(index);
            loadList(recordArrayList);
            table.clearSelection();

            editModeJRadioButtonMenuItem.setSelected(true);
            isEditableLable.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage("NSImage://NSLockUnlockedTemplate")));

            if (index >= 0 && recordArrayList.size() > 0) {
                if (index < recordArrayList.size()) {
                    table.setRowSelectionInterval(index, index);
                } else {
                    table.setRowSelectionInterval(recordArrayList.size() - 1, recordArrayList.size() - 1);
                }
            }
        }
        setEdited(true);
    }

    public void setEdited(boolean isFileEdited) {
        if (isEdited != isFileEdited) {
            isEdited = isFileEdited;
            getRootPane().putClientProperty("Window.documentModified", isFileEdited);
            setTitle(Values.DEFAULT_STORAGE_FILE_NAME + " - Изменено");
        }
    }

    public void updateTitle(File file) {
        getRootPane().putClientProperty("Window.documentFile", file);
        setTitle(file.getName());
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
