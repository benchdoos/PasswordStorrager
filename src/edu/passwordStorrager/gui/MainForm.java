package edu.passwordStorrager.gui;

import edu.passwordStorrager.core.Core;
import edu.passwordStorrager.core.Main;
import edu.passwordStorrager.core.PropertiesManager;
import edu.passwordStorrager.objects.Record;
import edu.passwordStorrager.protector.Values;
import edu.passwordStorrager.utils.FrameUtils;
import edu.passwordStorrager.utils.StringUtils;
import edu.passwordStorrager.utils.platform.PlatformUtils;
import edu.passwordStorrager.xmlManager.XmlParser;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventObject;

import static edu.passwordStorrager.core.Application.IS_MAC;
import static edu.passwordStorrager.core.Application.IS_WINDOWS;
import static edu.passwordStorrager.utils.FrameUtils.*;

public class MainForm extends JFrame {

    private static final Logger log = Logger.getLogger(getCurrentClassName());

    public static final int STATUS_MESSAGE = 1, STATUS_ERROR = -1, STATUS_SUCCESS = 2;
    protected static JRadioButtonMenuItem editModeJRadioButtonMenuItem; //if checked - can edit existing
    static Timer timer;

    static final String NUMBER_COLUMN_NAME = "#",
            SITE_COLUMN_NAME = "Сайт",
            LOGIN_COLUMN_NAME = "Логин",
            PASSWORD_COLUMN_NAME = "Пароль";

    private boolean isEdited = false;

    public ArrayList<Record> recordArrayList = new ArrayList<>();


    private JPopupMenu popupMenu;
    public static JMenuBar jMenuBar1;
    public static boolean isFirstLaunch = true;
    private JMenu fileJMenu;
    private JMenuItem openItem;
    private JMenuItem saveItem;
    private JMenuItem settingsItem;
    private JMenu editJMenu;
    private JMenuItem addItem;
    private JMenuItem addSomeItem;
    private JMenuItem deleteItem;
    private JMenuItem deleteSomeItem;
    private JMenuItem addUpItem;
    private JMenuItem addDownItem;
    private JMenuItem moveUpItem;
    private JMenuItem moveDownItem;
    private JMenuItem searchMenuItem;
    private JMenu copyJMenu;
    private JMenuItem copySiteItem;
    private JMenuItem copyLoginItem;
    private JMenuItem copyPasswordItem;
    private JMenu aboutItem;

    private JPanel panel1;
    private JScrollPane scrollPane;
    private JTable table;
    private JPanel statusPanel;
    private JTextField searchField;
    private JButton moveUpButton;
    private JButton moveDownButton;
    private JButton addUpButton;
    private JButton addDownButton;

    private JPanel controlPanel;
    private JProgressBar progressBar;
    private JLabel bar;
    private JProgressBar statusProgressBar;
    private JLabel rowCount;
    private JLabel isEditableIcon;

    private Timer searchTimer;
    private static TableModelListener tableModelListener;
    private boolean isSearchMode = false;


    public MainForm(ArrayList<Record> recordArrayList) {
        this.recordArrayList = recordArrayList;
        initComponents();
        requestFocus();
        table.requestFocus();
        Main.framesMainForm.add(this);
    }

    private void initComponents() {
        updateTitle(new File(Main.propertiesApplication.getProperty(PropertiesManager.KEY_NAME) + Values.DEFAULT_STORAGE_FILE_NAME));
        setContentPane(panel1);
        setIconImage(PlatformUtils.appIcon);

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        //setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(600, 430));
        setPreferredSize(getFrameSize(getCurrentClassName()));
        setLocation(getFrameLocation(getCurrentClassName()));

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (isEdited) {
                    //TODO ask before close
                }
                FrameUtils.setFrameLocation(getClass().getEnclosingClass().getName(), getLocation());
                FrameUtils.setFrameSize(getClass().getEnclosingClass().getName(), getSize());
                Core.onQuit();
            }

            public void windowClosed(WindowEvent e) {
                FrameUtils.setFrameLocation(getClass().getEnclosingClass().getName(), getLocation());
                FrameUtils.setFrameSize(getClass().getEnclosingClass().getName(), getSize());
                Core.onQuit();
            }
        });

        initTableListeners();

        initMenu();

        initPopUp();

        initControlBar();

        initSearchBarListeners();

        initStatusBar();

        loadList(recordArrayList);

        initTable();

        //request focus on table

        pack();
        if (table.getRowCount() > 0) {
            table.setRowSelectionInterval(0, 0);
        }
        isFirstLaunch = false; //to fix isEdited on start
    }

    private void initTable() {
        TableColumn number = table.getColumnModel().getColumn(0);
        number.setHeaderValue(NUMBER_COLUMN_NAME);
        number.setMinWidth(20);
        number.setMaxWidth(40);
        number.setPreferredWidth(number.getPreferredWidth());
        number.sizeWidthToFit();

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
//        scrollPane.setCorner(ScrollPaneConstants.UPPER_RIGHT_CORNER, null); //square, квадрат между table и scrollpane
    }


    public void setStatus(String status, int type) {
        showStatusBar(true);
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

    private void showStatusBar(boolean value) {
        if (value) {
            statusPanel.setMinimumSize(new Dimension(-1, 17));
            statusPanel.setMaximumSize(new Dimension(-1, 17));
            statusPanel.setPreferredSize(new Dimension(-1, 17));
            statusPanel.invalidate();
            getContentPane().validate();
        } else {
            statusPanel.setMinimumSize(new Dimension(-1, 2));
            statusPanel.setMaximumSize(new Dimension(-1, 2));
            statusPanel.setPreferredSize(new Dimension(-1, 2));
            statusPanel.invalidate();
            getContentPane().validate();
        }
    }

    public void resetStatus() {
        bar.setForeground(Color.black);
        bar.setText("");
        showStatusBar(false);
    }

    private void updateRowCount(int count) {
        rowCount.setText("Записей: " + count);
    }


    private void initSearchBarListeners() {
        /*panel1.registerKeyboardAction(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        searchField.requestFocus();
                    }
                }, KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.META_MASK),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);*/

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
                searchField.setMaximumSize(new Dimension(width, height));
                searchField.invalidate();
                controlPanel.validate();
            }

            @Override
            public void focusGained(FocusEvent e) {
                int width = 300;
                changeSearchFieldSize(width);

                System.out.println("gained");
            }

            @Override
            public void focusLost(FocusEvent e) {
                int width = 150;
                changeSearchFieldSize(width);
                System.out.println("lost");
            }
        });

        ActionListener clearSearchFieldAction = new ActionListener() {
            void clear() {
                if (!searchField.getText().isEmpty()) {
                    searchField.setText("");
                    loadList(recordArrayList);
                    setControlButtonsEnabled(true);
                    isSearchMode = false;
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
        if (IS_MAC) {
            searchField.registerKeyboardAction(clearSearchFieldAction, KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE,
                    InputEvent.META_MASK), JComponent.WHEN_FOCUSED);
        } else if (IS_WINDOWS) {
            searchField.registerKeyboardAction(clearSearchFieldAction, KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE,
                    InputEvent.CTRL_MASK), JComponent.WHEN_FOCUSED);
        }
    }

    private void initSearchTimer(final String text) {
        if (searchTimer != null) {
            searchTimer.stop();
        }
        searchTimer = new Timer(500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isSearchMode = true;

                progressBar.setVisible(true);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        loadList(searchRecord(text));
                    }
                }).start();
                setControlButtonsEnabled(false);

            }
        });
        searchTimer.setRepeats(false);
        searchTimer.start();
    }

    private void setControlButtonsEnabled(boolean value) {
        addUpButton.setEnabled(value);
        addDownButton.setEnabled(value);
        moveUpButton.setEnabled(value);
        moveDownButton.setEnabled(value);
    }

    private ArrayList<Record> searchRecord(String text) {
        if (text.isEmpty()) {
            return recordArrayList;
        }
        //start
        progressBar.setValue(0);

        ArrayList<Record> foundRecords = new ArrayList<>(recordArrayList.size());

        int total = recordArrayList.size();
        int current = 0;

        for (Record record : recordArrayList) {
            current++;
            try {
                if (record.getSite().contains(text) || record.getLogin().contains(text)) {
                    foundRecords.add(record);
                }
            } catch (NullPointerException ignored) {
            }
            progressBar.setValue((int) ((double) current / total) * 100);
        }
        return foundRecords;
    }

    private void initControlBar() {
        addUpButton.putClientProperty("JButton.buttonType", "gradient");
        addDownButton.putClientProperty("JButton.buttonType", "gradient");
        moveUpButton.putClientProperty("JButton.buttonType", "gradient");
        moveDownButton.putClientProperty("JButton.buttonType", "gradient");
        /*addUpButton.putClientProperty("JButton.buttonType", "textured");
        addDownButton.putClientProperty("JButton.buttonType", "textured");*/

        addUpButton.setText("");
        addUpButton.setToolTipText("Добавить запись сверху");
        addDownButton.setText("");
        addDownButton.setToolTipText("Добавить запись снизу");
        moveUpButton.setText("");
        moveUpButton.setToolTipText("Переместить вверх");
        moveDownButton.setText("");
        moveDownButton.setToolTipText("Переместить вниз");
        try {
            Image img = ImageIO.read(getClass().getResource("/icons/controls/AddDown.png"));
            addDownButton.setIcon(new ImageIcon(img));
            img = ImageIO.read(getClass().getResource("/icons/controls/AddUp.png"));
            addUpButton.setIcon(new ImageIcon(img));
            img = ImageIO.read(getClass().getResource("/icons/controls/moveUp.png"));
            moveUpButton.setIcon(new ImageIcon(img));
            img = ImageIO.read(getClass().getResource("/icons/controls/moveDown.png"));
            moveDownButton.setIcon(new ImageIcon(img));
        } catch (IOException ex) {
            log.warn("Can not load images for buttons");
        }

        /////Listeners
        addUpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = table.getSelectedRow();
                if (index > 0) {
                    addNewRecord(index, 1);
                } else {
                    addNewRecord(0, 1);
                }
            }
        });
        addDownButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = table.getSelectedRow();
                if (index > -1) {
                    addNewRecord(index + 1, 1);
                } else {
                    addNewRecord(table.getRowCount(), 1);
                }
            }
        });

        moveUpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = table.getSelectedRow();
                if (index > 0) {
                    exchangeRecords(index, index - 1);
                    table.clearSelection();
                    table.setRowSelectionInterval(index - 1, index - 1);
                }
            }
        });

        moveDownButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = table.getSelectedRow();
                if (index > -1) {
                    if (index + 1 < table.getRowCount()) {
                        exchangeRecords(index, index + 1);
                        table.clearSelection();
                        table.setRowSelectionInterval(index + 1, index + 1);
                    }
                }
            }
        });

        progressBar.setIndeterminate(true);
        progressBar.putClientProperty("JProgressBar.style", "circular");
        progressBar.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JProgressBar pb = (JProgressBar) e.getSource();
                pb.setToolTipText(pb.getValue() + "%");

                pb.setVisible(!(pb.getValue() == 100));

            }
        });
    }

    private void initStatusBar() {
        bar.setFont(new Font("Menlo", Font.PLAIN, 10));
        bar.setForeground(Color.black);
        bar.setVerticalAlignment(JLabel.TOP);
        rowCount.setFont(new Font("Menlo", Font.PLAIN, 10));
        rowCount.setVerticalAlignment(JLabel.TOP);

        statusProgressBar.setIndeterminate(true);
        statusProgressBar.putClientProperty("JProgressBar.style", "circular");

        statusPanel.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {
                showStatusBar(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                //showStatusBar(false);
                //setStatus(bar.getText(),STATUS_MESSAGE);
            }
        });
    }

    private void initTableListeners() {
        MouseListener copyMouseListener = new MouseAdapter() {
            int count = 0;
            Timer timer = new Timer(700, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    count = 0;
                }
            });

            public void mousePressed(MouseEvent me) {
                count = me.getClickCount();
                if (timer.isRunning()) {
                    timer.restart();
                } else {
                    timer.start();
                }
                JTable table = (JTable) me.getSource();
                Point p = me.getPoint();
                int row = table.rowAtPoint(p);
                if (count >= 2 && me.getButton() == MouseEvent.BUTTON1) {
                    if (MainForm.this.table.getSelectedRow() >= 0) {
                        if (MainForm.this.table.getSelectedColumn() == table.getColumn(SITE_COLUMN_NAME).getModelIndex()) {
                            java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
                            String site = (String) MainForm.this.table.getModel()
                                    .getValueAt(row, table.getColumn(SITE_COLUMN_NAME).getModelIndex());
                            try {
                                if (StringUtils.isUrl(site)) {
                                    desktop.browse(URI.create(StringUtils.parseUrl(site)));
                                }
                            } catch (IOException e) {
                                log.warn("Can not open in browser: " + StringUtils.parseUrl(site));
                            }
                        } else {
                            String copy = (String) MainForm.this.table.getModel().getValueAt(row,
                                    table.getColumn(PASSWORD_COLUMN_NAME).getModelIndex());
                            copyToClipboard(copy);
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
                    //TODO what do i need here?
                } else {
                    int kEvent = -1;
                    if (IS_MAC) {
                        kEvent = KeyEvent.META_MASK;
                    } else if (IS_WINDOWS) {
                        kEvent = KeyEvent.CTRL_MASK;
                    }
                    if ((e.getKeyCode() == KeyEvent.VK_C) && ((e.getModifiers() & kEvent) != 0)) {
                        if (table.getSelectedRow() != -1) {
                            String copy = (String) MainForm.this.table.getModel().getValueAt(table.getSelectedRow(),
                                    table.getColumn(PASSWORD_COLUMN_NAME).getModelIndex());
                            copyToClipboard(copy);
                        }
                    }
                    //TODO FIX IF NEEDED
                    int key = e.getKeyCode();

                    if ((((key >= 65) && (key <= 90)) || ((key >= 97) && (key <= 122)) || ((key >= 48) && (key <= 57))) && e.getModifiers() <= 0) {
                        searchField.requestFocus();
                        searchField.setText(e.getKeyChar() + "");
                        searchField.setCaret(new DefaultCaret());
                        searchField.setCaretPosition(searchField.getText().length());
                    }

                    if (e.getKeyCode() == KeyEvent.VK_ESCAPE && isSearchMode) {
                        searchField.requestFocus();
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });

        ListSelectionModel cellSelectionModel = table.getSelectionModel();
        cellSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cellSelectionModel.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                int selected = table.getSelectedRow();
                if (table.isFocusOwner()) {
                    if (selected > -1) {
                        if (!isSearchMode) {
                            if (selected == 0) {
                                moveUpButton.setEnabled(false);
                                moveUpItem.setEnabled(false);
                            } else {
                                moveUpButton.setEnabled(true);
                                moveUpItem.setEnabled(true);
                            }
                            if (selected == table.getRowCount() - 1) {
                                moveDownButton.setEnabled(false);
                                moveDownItem.setEnabled(false);
                            } else {
                                moveDownButton.setEnabled(true);
                                moveDownItem.setEnabled(true);
                            }
                        }
                    }

                }

            }
        });

        tableModelListener = new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                int row = table.getEditingRow();
                int col = table.getEditingColumn();
                String value = (String) table.getValueAt(row, col);

                Record oldRec = recordArrayList.get(row);
                Record newRec = new Record();
                newRec.setSite(oldRec.getSite());
                newRec.setLogin(oldRec.getLogin());
                newRec.setPassword(oldRec.getPassword());
                if (col == table.getColumn(SITE_COLUMN_NAME).getModelIndex()) {
                    newRec.setSite(value);
                }
                if (col == table.getColumn(LOGIN_COLUMN_NAME).getModelIndex()) {
                    newRec.setLogin(value);
                }

                if (col == table.getColumn(PASSWORD_COLUMN_NAME).getModelIndex()) {
                    newRec.setPassword(value);
                }
                recordArrayList.set(row, newRec);

                if (isFirstLaunch) {
                    setEdited(false);
                } else {
                    setEdited(true);
                }
            }
        };

        table.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (!isSearchMode) {
                    setControlButtonsEnabled(true);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                setControlButtonsEnabled(false);
            }
        });
        //TODO add table change listener, fix the caret
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
        addSomeItem = new JMenuItem();
        deleteItem = new JMenuItem();
        deleteSomeItem = new JMenuItem();
        addUpItem = new JMenuItem();
        addDownItem = new JMenuItem();
        moveUpItem = new JMenuItem();
        moveDownItem = new JMenuItem();
        searchMenuItem = new JMenuItem();

        copyJMenu = new JMenu();
        copySiteItem = new JMenuItem();
        copyLoginItem = new JMenuItem();
        copyPasswordItem = new JMenuItem();

        aboutItem = new JMenu();

        fileJMenu.setText("Файл");

        openItem.setAccelerator(getAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.META_MASK),
                KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK)));
        openItem.setText("Открыть");
        openItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isSearchMode) {
                    recordArrayList = new XmlParser().parseRecords();
                    loadList(recordArrayList);
                    setEdited(false);
                    editModeJRadioButtonMenuItem.setSelected(false);
                    isEditableIcon.setIcon(new ImageIcon(getClass().getResource("/icons/controls/lock.png")));
                    isEditableIcon.setToolTipText("Режим редактирования выключен");
                }
            }
        });
        fileJMenu.add(openItem);

        saveItem.setAccelerator(getAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.META_MASK),
                KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK)));

        saveItem.setText("Сохранить");
        saveItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isSearchMode) {
                    saveStorage();
                }
            }
        });
        fileJMenu.add(saveItem);

        jMenuBar1.add(fileJMenu);

        settingsItem.setAccelerator(getAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, InputEvent.META_MASK),
                KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, InputEvent.CTRL_MASK)));

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

        if (IS_WINDOWS) {
            fileJMenu.add(settingsItem);
        }

        editJMenu.setText("Правка");

        editModeJRadioButtonMenuItem.setAccelerator(getAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.META_MASK),
                KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK)));

        editModeJRadioButtonMenuItem.setSelected(false);
        editModeJRadioButtonMenuItem.setText("Режим редактирования");
        editModeJRadioButtonMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isSearchMode) {
                    if (!editModeJRadioButtonMenuItem.isSelected()) {
                        isEditableIcon.setIcon(new ImageIcon(getClass().getResource("/icons/controls/lock.png")));
                        isEditableIcon.setToolTipText("Режим редактирования выключен");
                        int index = table.getSelectedRow();
                        try {
                            if (table.isEditing()) {
                                table.getCellEditor().cancelCellEditing();
                            }
                        } catch (NullPointerException ignored) {
                        }
//                    table.getCellEditor(table.getEditingRow(), table.getEditingColumn()).cancelCellEditing();
                        if (index > -1) {
                            table.setRowSelectionInterval(index, index);
                        }
                        //table.clearSelection();
                    } else {
                        isEditableIcon.setIcon(new ImageIcon(getClass().getResource("/icons/controls/unlock.png")));
                        isEditableIcon.setToolTipText("Режим редактирования включен");
                    }
                }
            }
        });

        isEditableIcon.setIcon(new ImageIcon(getClass().getResource("/icons/controls/lock.png")));
        editJMenu.add(editModeJRadioButtonMenuItem);

        addItem.setAccelerator(getAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.META_MASK),
                KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK)));

        addItem.setText("Добавить");
        addItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isSearchMode) {
                    addNewRecord(recordArrayList.size(), 1);
                }
            }
        });
        editJMenu.add(addItem);

        addSomeItem.setAccelerator(getAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.META_MASK | InputEvent.SHIFT_MASK),
                KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK)));

        addSomeItem.setText("Добавить несколько...");
        addSomeItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isSearchMode) {
                    new InputForm("Добавить") {
                        @Override
                        void onOK() {
                            int row = table.getSelectedRow();
                            int count = 0;
                            try {
                                count = Integer.parseInt(this.value.getText());
                            } catch (NumberFormatException ignored) {/*NOP*/}

                            if (count > 0 && count <= 1000) {
                                if (table.getRowCount() < 1) {
                                    addNewRecord(0, count);
                                } else {
                                    addNewRecord(row + 1, count); //after selection //row = before
                                }
                                dispose();
                            } else {
                                shakeFrame(this);
                                restore();
                            }

                        }
                    }.setVisible(true);
                }
            }
        });
        editJMenu.add(addSomeItem);

        deleteItem.setAccelerator(getAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, InputEvent.META_MASK),
                KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, InputEvent.CTRL_MASK)));

        deleteItem.setText("Удалить");
        deleteItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isSearchMode) {
                    deleteSelectedRecords(table.getSelectedRow(), table.getSelectedRow());
                }
            }
        });
        editJMenu.add(deleteItem);

        deleteSomeItem.setAccelerator(getAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, InputEvent.META_MASK | InputEvent.SHIFT_MASK),
                KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK)));
        deleteSomeItem.setText("Удалить несколько...");
        deleteSomeItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isSearchMode) {
                    new InputForm("Удалить") {
                        @Override
                        void onOK() {
                            try {
                                int index1;
                                int index2;
                                if (value.getText().length() == 1 && value.getText().contains("*")) {
                                    recordArrayList = new ArrayList<>();
                                    loadList(recordArrayList);
                                } else {
                                    String[] values = value.getText().split("-");
                                    index1 = Integer.parseInt(values[0]);
                                    index1--;
                                    try {
                                        index2 = Integer.parseInt(values[1]);
                                        index2--;
                                    } catch (ArrayIndexOutOfBoundsException e) {
                                        index2 = table.getSelectedRow() + index1;
                                        index1 = table.getSelectedRow();
                                    }
                                    deleteSelectedRecords(index1, index2);
                                }
                                dispose();
                            } catch (NumberFormatException e) {
                                shakeFrame(this);
                                restore();
                            }

                        }
                    }.setVisible(true);
                }
            }
        });
        editJMenu.add(deleteSomeItem);

        addUpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.ALT_MASK | InputEvent.SHIFT_MASK));
        addUpItem.setText("Добавить запись сверху");
        addUpItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addUpButton.doClick();
            }
        });
        editJMenu.add(addUpItem);

        addDownItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.ALT_MASK | InputEvent.SHIFT_MASK));
        addDownItem.setText("Добавить запись снизу");
        addDownItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addDownButton.doClick();
            }
        });
        editJMenu.add(addDownItem);

        moveUpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.CTRL_MASK | InputEvent.ALT_MASK | InputEvent.SHIFT_MASK));
        moveUpItem.setText("Переместить вверх");
        moveUpItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moveUpButton.doClick();
            }
        });
        editJMenu.add(moveUpItem);

        moveDownItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.CTRL_MASK | InputEvent.ALT_MASK | InputEvent.SHIFT_MASK));
        moveDownItem.setText("Переместить вниз");
        moveDownItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moveDownButton.doClick();
            }
        });
        editJMenu.add(moveDownItem);

        searchMenuItem.setAccelerator(getAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.META_MASK),
                KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK)));

        searchMenuItem.setText("Поиск");
        searchMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchField.requestFocus();
            }
        });
        editJMenu.add(searchMenuItem);

        jMenuBar1.add(editJMenu);

        copyJMenu.setText("Копировать");

        copySiteItem.setAccelerator(getAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, InputEvent.META_MASK),
                KeyStroke.getKeyStroke(KeyEvent.VK_1, InputEvent.CTRL_MASK)));

        copySiteItem.setText("Копировать сайт");
        copySiteItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copySelectedCell(table.getColumn(SITE_COLUMN_NAME).getModelIndex());
            }
        });
        copyJMenu.add(copySiteItem);

        copyLoginItem.setAccelerator(getAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, InputEvent.META_MASK),
                KeyStroke.getKeyStroke(KeyEvent.VK_2, InputEvent.CTRL_MASK)));

        copyLoginItem.setText("Копировать логин");
        copyLoginItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copySelectedCell(table.getColumn(LOGIN_COLUMN_NAME).getModelIndex());
            }
        });
        copyJMenu.add(copyLoginItem);

        copyPasswordItem.setAccelerator(getAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, InputEvent.META_MASK),
                KeyStroke.getKeyStroke(KeyEvent.VK_3, InputEvent.CTRL_MASK)));

        copyPasswordItem.setText("Копировать пароль");
        copyPasswordItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copySelectedCell(table.getColumn(PASSWORD_COLUMN_NAME).getModelIndex());
            }
        });
        copyJMenu.add(copyPasswordItem);

        jMenuBar1.add(copyJMenu);

        aboutItem.setText("О программе");
        aboutItem.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(MenuEvent e) {
                new AboutApplication();
            }

            @Override
            public void menuDeselected(MenuEvent e) {
            }

            @Override
            public void menuCanceled(MenuEvent e) {
            }
        });

        if (IS_WINDOWS) {
            jMenuBar1.add(aboutItem);
        }

        setJMenuBar(jMenuBar1);
    }

    private KeyStroke getAccelerator(KeyStroke mac, KeyStroke windows) {
        if (IS_MAC) {
            return mac;
        } else if (IS_WINDOWS) {
            return windows;
        } else {
            //TODO edit if deeded
            return windows;
        }
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
                String copy = (String) table.getModel().getValueAt(table.getSelectedRow(), table.getColumn(SITE_COLUMN_NAME).getModelIndex());
                copyToClipboard(copy);
            }
        });
        menuItemCopyLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (table.getSelectedRow() != -1) {
                    String copy = (String) table.getModel().getValueAt(table.getSelectedRow(), table.getColumn(LOGIN_COLUMN_NAME).getModelIndex());
                    copyToClipboard(copy);
                    setStatus("Скопировано: " + copy, STATUS_SUCCESS);
                }
            }
        });

    }

    private void copyToClipboard(String copy) {
        if (copy != null) {
            if (!copy.isEmpty()) {
                FrameUtils.copyToClipboard(copy);
                setStatus("Скопировано: " + copy, STATUS_SUCCESS);
            } else {
                setStatus("Нечего копировать!", STATUS_ERROR);
            }
        } else {
            setStatus("Нечего копировать!", STATUS_ERROR);
        }
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
            String copy = (String) table.getModel().getValueAt(table.getSelectedRow(), column);
            copyToClipboard(copy);
            setStatus("Скопировано: " + copy, STATUS_SUCCESS);
        }
    }

    public void loadList(ArrayList<Record> recordArrayList) {
        //Record[] recordsList = recordArrayList.toArray(new Record[recordArrayList.size()]);
        statusProgressBar.setVisible(true);
        setStatus(bar.getText(), STATUS_MESSAGE);
        table.setModel(createTableModel(recordArrayList));
        table.setRowHeight(25);

        initTable();

        table.getColumn(NUMBER_COLUMN_NAME).setCellEditor(new TableEditor(new JTextField(NUMBER_COLUMN_NAME)));
        table.getColumn(SITE_COLUMN_NAME).setCellEditor(new TableEditor(new JTextField(SITE_COLUMN_NAME)));
        table.getColumn(LOGIN_COLUMN_NAME).setCellEditor(new TableEditor(new JTextField(LOGIN_COLUMN_NAME)));
        table.getColumn(PASSWORD_COLUMN_NAME).setCellEditor(new TableEditor(new JTextField(PASSWORD_COLUMN_NAME)));

        resizeTableColumns(table.getColumn(NUMBER_COLUMN_NAME));

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setDragEnabled(false);
        table.getTableHeader().setReorderingAllowed(false); //to prevent column dragging(moving)

        table.setComponentPopupMenu(popupMenu);
        table.setSurrendersFocusOnKeystroke(true);
        table.getModel().addTableModelListener(tableModelListener);
        statusProgressBar.setVisible(false);

        updateRowCount(recordArrayList.size());
        setStatus("Количество записей: " + table.getModel().getRowCount(), STATUS_MESSAGE);
    }

    private void saveStorage() {
        int rows = table.getRowCount();
        System.out.println("rows to save:" + rows);
        editModeJRadioButtonMenuItem.doClick();
        recordArrayList = new ArrayList<>(rows);
        for (int i = 0; i < rows; i++) {
            Record record = new Record();
            record.setSite((String) table.getModel().getValueAt(i, table.getColumn(SITE_COLUMN_NAME).getModelIndex()));
            record.setLogin((String) table.getModel().getValueAt(i, table.getColumn(LOGIN_COLUMN_NAME).getModelIndex()));
            record.setPassword((String) table.getModel().getValueAt(i, table.getColumn(PASSWORD_COLUMN_NAME).getModelIndex()));
            recordArrayList.add(record);
        }
        new XmlParser().saveRecords(recordArrayList);
        loadList(recordArrayList);
        setEdited(false);
        setStatus("Сохранено", STATUS_SUCCESS);
    }

    private void addNewRecord(int index, int count) {
        if (!editModeJRadioButtonMenuItem.isSelected()) {
            editModeJRadioButtonMenuItem.doClick();
        }
        setStatus(bar.getText(), STATUS_MESSAGE);
        isEditableIcon.setIcon(new ImageIcon(getClass().getResource("/icons/controls/unlock.png")));

        if (count > 100) {
            Record[] tmp = new Record[recordArrayList.size()];
            tmp = recordArrayList.toArray(tmp);

            Record[] rec1 = Arrays.copyOfRange(tmp, 0, index);

            Record[] rec2 = new Record[count];
            for (int i = 0; i < count; i++) {
                rec2[i] = new Record();
                statusProgressBar.setValue((int) ((double) i / count) * 100);
                statusProgressBar.setToolTipText(statusProgressBar.getValue() + "%");
            }
            Record[] rec3 = Arrays.copyOfRange(tmp, index, tmp.length);
            Record[] rec;
            rec = concatenate(rec1, rec2);
            rec = concatenate(rec, rec3);

            recordArrayList = new ArrayList<>(Arrays.asList(rec));
        } else {
            for (int i = 0; i < count; i++) {
                recordArrayList.add(index,new Record());
            }
        }
        loadList(recordArrayList);

        table.clearSelection();
        table.setRowSelectionInterval(index, index);
        scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
        setEdited(true);
    }

    private void exchangeRecords(int index1, int index2) {
        Record rec1 = recordArrayList.get(index1);
        Record rec2 = recordArrayList.get(index2);
        recordArrayList.set(index1, rec2);
        recordArrayList.set(index2, rec1);
        loadList(recordArrayList);
        table.clearSelection();
        table.setRowSelectionInterval(index1, index1);
        setEdited(true);
    }

    private void deleteSelectedRecords(int index1, int index2) {
//index1 - меньше index2
        System.out.println("before:" + index1 + " " + index2);
        if (index1 > index2) {
            int i = index1;
            index1 = index2;
            index2 = i;
        }

        if (index1 < 0) {
            index1 = 0;
        }
        if (index2 > table.getRowCount() - 1) {
            index2 = table.getRowCount() - 1;
        }

        System.out.println("after:" + index1 + " " + index2);

        if (recordArrayList.size() > 0) {
            int diff = index2 - index1;
            recordArrayList.remove(index1);
            for (int i = 0; i < diff; i++) {
                recordArrayList.remove(index1);
            }
            loadList(recordArrayList);
            table.clearSelection();

            editModeJRadioButtonMenuItem.setSelected(true);
            isEditableIcon.setIcon(new ImageIcon(getClass().getResource("/icons/controls/unlock.png")));

            if (index1 >= 0) {
                if (index1 < recordArrayList.size()) {
                    table.setRowSelectionInterval(index1, index1);
                }
            }
        }

        //int index = table.getSelectedRow();
        /*if (index >= 0) {
            recordArrayList.remove(index);
            loadList(recordArrayList);
            table.clearSelection();

            editModeJRadioButtonMenuItem.setSelected(true);
            isEditableIcon.setIcon(new ImageIcon(getClass().getResource("/icons/controls/unlock.png")));

            if (index >= 0 && recordArrayList.size() > 0) {
                if (index < recordArrayList.size()) {
                    table.setRowSelectionInterval(index, index);
                } else {
                    table.setRowSelectionInterval(recordArrayList.size() - 1, recordArrayList.size() - 1);
                }
            }
        }*/
        setEdited(true);
    }

    public void setEdited(boolean isFileEdited) {
        isEdited = isFileEdited;
        if (isFileEdited) {
            setTitle(Values.DEFAULT_STORAGE_FILE_NAME + " - Изменено");
        } else {
            setTitle(Values.DEFAULT_STORAGE_FILE_NAME);
        }
        getRootPane().putClientProperty("Window.documentModified", isEdited);
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

        int total = recordArrayList.size();

        for (int i = 0; i < recordArrayList.size(); i++) {
            number[i] = (i + 1) + "";
            siteData[i] = recordArrayList.get(i).getSite();
            loginData[i] = recordArrayList.get(i).getLogin();
            pwdData[i] = recordArrayList.get(i).getPassword();
            statusProgressBar.setValue((int) ((double) i / total) * 100);
            statusProgressBar.setToolTipText(statusProgressBar.getValue() + "%");
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
            return textField != null && !textField.getText().equals(NUMBER_COLUMN_NAME)
                    && editModeJRadioButtonMenuItem.isSelected();
        }
    }


    static <T> T[] concatenate(T[] a, T[] b) {
        int aLen = a.length;
        int bLen = b.length;

        @SuppressWarnings("unchecked")
        T[] c = (T[]) Array.newInstance(a.getClass().getComponentType(), aLen + bLen);
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);

        return c;
    }
}


abstract class InputForm extends JDialog {
    JPanel contentPane = new JPanel();
    JTextField value = new JTextField(4);
    JButton buttonOK = new JButton();
    JProgressBar progressBar = new JProgressBar();

    public InputForm(String title) {
        setModal(true);
        setResizable(false);
        setSize(new Dimension(50, 40));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setTitle(title);
        getRootPane().putClientProperty("Window.style", "small");
        setLayout(new BorderLayout());
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        getRootPane().setDefaultButton(buttonOK);
        buttonOK.setText(title);

        progressBar.setIndeterminate(true);
        progressBar.putClientProperty("JProgressBar.style", "circular");
        progressBar.setVisible(false);

        contentPane.add(new JLabel(title + ":"));
        contentPane.add(value);
        buttonOK.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                progressBar.setVisible(true);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        buttonOK.setVisible(false);
                        value.setEnabled(false);
                        onOK();
                    }
                }).start();
            }
        });
        contentPane.add(progressBar);
        contentPane.add(buttonOK);
        add(contentPane);

        pack();
        setLocation(FrameUtils.setFrameOnCenter(getSize()));
    }

    abstract void onOK();

    public void restore() {
        progressBar.setVisible(false);
        buttonOK.setVisible(true);
        value.setEnabled(true);

    }
}
