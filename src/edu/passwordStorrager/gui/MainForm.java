package edu.passwordStorrager.gui;

import edu.passwordStorrager.core.Application;
import edu.passwordStorrager.core.Core;
import edu.passwordStorrager.core.PasswordStorrager;
import edu.passwordStorrager.core.PropertiesManager;
import edu.passwordStorrager.objects.Record;
import edu.passwordStorrager.protector.Values;
import edu.passwordStorrager.utils.FrameUtils;
import edu.passwordStorrager.utils.StringUtils;
import edu.passwordStorrager.utils.history.History;
import edu.passwordStorrager.utils.history.actions.AddRowAction;
import edu.passwordStorrager.utils.history.actions.ChangeCellValueAction;
import edu.passwordStorrager.utils.history.actions.ExchangedRowsAction;
import edu.passwordStorrager.utils.history.actions.RemoveRowAction;
import edu.passwordStorrager.utils.platform.MacOsXUtils;
import edu.passwordStorrager.utils.platform.PlatformUtils;
import edu.passwordStorrager.xmlManager.XmlParser;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.Timer;
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
import java.util.*;

import static edu.passwordStorrager.core.Application.*;
import static edu.passwordStorrager.utils.FrameUtils.*;

public class MainForm extends JFrame {

    private static final Logger log = Logger.getLogger(getCurrentClassName());

    public static final int STATUS_MESSAGE = 1, STATUS_ERROR = -1, STATUS_SUCCESS = 2;
    protected static JRadioButtonMenuItem editModeJRadioButtonMenuItem; //if checked - can edit existing
    static Timer timer;

    static final String NUMBER_COLUMN_NAME = "#", SITE_COLUMN_NAME = "Сайт",
            LOGIN_COLUMN_NAME = "Логин", PASSWORD_COLUMN_NAME = "Пароль";

    static int NUMBER_COLUMN_INDEX = 0;
    static int SITE_COLUMN_INDEX = 1;
    static int LOGIN_COLUMN_INDEX = 2;
    static int PASSWORD_COLUMN_INDEX = 3;

    private History history;

    private boolean isEdited = false;

    public ArrayList<Record> recordArrayList = new ArrayList<>();


    private JPopupMenu popupMenu;
    public static boolean isFirstLaunch = true;
    public JMenuBar jMenuBar1 = new JMenuBar();
    ;
    private JMenu fileJMenu = new JMenu("Файл");
    private JMenuItem openItem = new JMenuItem("Открыть");
    private JMenuItem saveItem = new JMenuItem("Сохранить");
    private JMenuItem blockItem = new JMenuItem("Блокировать");
    private JMenuItem settingsItem = new JMenuItem("Настройки");
    private JMenu editJMenu = new JMenu("Правка");

    public JMenuItem undoItem = new JMenuItem("Отменить");
    public JMenuItem redoItem = new JMenuItem("Повторить");

    private JMenuItem addItem = new JMenuItem("Добавить");
    private JMenuItem addSomeItem = new JMenuItem("Добавить несколько...");
    private JMenuItem deleteItem = new JMenuItem("Удалить");
    private JMenuItem deleteSomeItem = new JMenuItem("Удалить несколько...");
    private JMenuItem addUpItem = new JMenuItem("Добавить запись сверху");
    private JMenuItem addDownItem = new JMenuItem("Добавить запись снизу");
    private JMenuItem moveUpItem = new JMenuItem("Переместить вверх");
    private JMenuItem moveDownItem = new JMenuItem("Переместить вниз");
    private JMenuItem searchMenuItem = new JMenuItem("Поиск");
    private JMenu copyJMenu = new JMenu("Копировать");
    private JMenuItem copySiteItem = new JMenuItem("Копировать сайт");
    private JMenuItem copyLoginItem = new JMenuItem("Копировать логин");
    private JMenuItem copyPasswordItem = new JMenuItem("Копировать пароль");
    private JMenu referenceMenu = new JMenu("Справка");
    private JMenuItem aboutItem = new JMenuItem("О программе");

    private JPanel panel1;
    private JScrollPane scrollPane;
    public ZebraJTable table;
    private JPanel statusPanel;
    private JTextField searchField;
    private JButton moveUpButton;
    private JButton moveDownButton;
    private JButton addUpButton;
    private JButton addDownButton;

    private JPanel controlPanel;
    private Point mouse = new Point(0, 0);
    private MouseListener controlPanelMouseListener;
    private MouseMotionAdapter controlPanelMouseMotionAdapter;

    private JProgressBar progressBar;
    private JLabel bar;
    private JLabel rowCount;
    private JLabel isEditableIcon;
    private JPanel searchPanel;
    private JLabel info;

    private Timer searchTimer;
    private static Timer lockTimer; //for multiple windows
    private static TableModelListener tableModelListener;
    private boolean isSearchMode = false;


    public MainForm(ArrayList<Record> recordArrayList) {
        this.recordArrayList = recordArrayList;
        initComponents();
        requestFocus();
        table.requestFocus();
        initHistory();
        PasswordStorrager.frames.add(this);
    }

    private void initComponents() {
        if (IS_MAC) {
            updateTitle(new File(PasswordStorrager.propertiesApplication.getProperty(PropertiesManager.KEY_NAME) + Values.DEFAULT_STORAGE_FILE_NAME));
        } else {
            setTitle(Application.APPLICATION_NAME);
        }
        setContentPane(panel1);
        if (!MacOsXUtils.isBundled()) {
            setIconImage(PlatformUtils.appIcon);
        }

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(600, 430));
        setPreferredSize(getFrameSize(getCurrentClassName()));
        setLocation(getFrameLocation(getCurrentClassName()));

        getRootPane().putClientProperty("apple.awt.brushMetalLook", Boolean.TRUE);

        isEditableIcon.setIcon(new ImageIcon(getClass().getResource("/icons/controls/lock.png")));

        initLockTimer();

        initWindowListeners();

        initScrollPaneListeners();

        initTableListeners();

        initMenu();

//        initPopUp();

        initControlBar();

        initSearchBarListeners();

        initStatusBar();

        loadList(recordArrayList);

        setStatus("Количество записей: " + table.getModel().getRowCount(), STATUS_MESSAGE);

        initTable();

        pack();

        if (table.getRowCount() > 0) {
            table.setRowSelectionInterval(0, 0);
            table.setColumnSelectionInterval(1, 1);
        }
        isFirstLaunch = false; //to fix isEdited on start
    }

    private void initLockTimer() {
        ActionListener actionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                blockItem.getActionListeners()[0].actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
            }
        };
        lockTimer = new Timer(60 * 1000, actionListener);
        lockTimer.setRepeats(false);
        refreshLockTimer();
    }

    private void initWindowListeners() {
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (isEdited) {
                    SaveOnExitDialog saveOnExitDialog = new SaveOnExitDialog();
                    if (IS_MAC) new MovingTogether((JFrame) e.getWindow(), saveOnExitDialog);
                    saveOnExitDialog.setVisible(true);
                } else {
                    disposeForm();
                }
                FrameUtils.setFrameLocation(getClass().getEnclosingClass().getName(), getLocation());
                FrameUtils.setFrameSize(getClass().getEnclosingClass().getName(), getSize());
            }

            public void disposeForm() {
                dispose();
                Core.onQuit();
            }
        });

        addWindowFocusListener(new WindowFocusListener() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                refreshLockTimer();
            }

            @Override
            public void windowLostFocus(WindowEvent e) {
                refreshLockTimer();
            }
        });
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

        NUMBER_COLUMN_INDEX = table.getColumn(NUMBER_COLUMN_NAME).getModelIndex();
        SITE_COLUMN_INDEX = table.getColumn(SITE_COLUMN_NAME).getModelIndex();
        LOGIN_COLUMN_INDEX = table.getColumn(LOGIN_COLUMN_NAME).getModelIndex();
        PASSWORD_COLUMN_INDEX = table.getColumn(PASSWORD_COLUMN_NAME).getModelIndex();
//        scrollPane.setCorner(ScrollPaneConstants.UPPER_RIGHT_CORNER, null); //square, квадрат между table и scrollpane
    }

    private void initSearchBarListeners() {

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
                isSearchMode = true;
                int width = 300;
                changeSearchFieldSize(width);
                refreshLockTimer();
            }

            @Override
            public void focusLost(FocusEvent e) {
                int width = 150;
                changeSearchFieldSize(width);
                if (searchField.getText().isEmpty()) {
                    isSearchMode = false;
                }
                refreshLockTimer();
            }
        });

        ActionListener clearSearchFieldAction = new ActionListener() {
            void clear() {
                if (!searchField.getText().isEmpty()) {
                    searchField.setText("");
                    loadList(recordArrayList);
                    setControlButtonsEnabled(true);
                    isSearchMode = false;
                    editModeJRadioButtonMenuItem.setEnabled(true);
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
                editModeJRadioButtonMenuItem.setEnabled(false);

                progressBar.setVisible(true);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //что то тут не так (при добавлении новых элементов - они не ищутся)
                        loadList(searchRecord(text));
                        setStatus("Найдено записей: " + table.getRowCount(), STATUS_MESSAGE);
                    }
                }).start();
                setControlButtonsEnabled(false);

            }
        });
        searchTimer.setRepeats(false);
        searchTimer.start();
    }


    public void setStatus(String status, int type) {
        resetStatus();
        //showStatusBar(true);
        switch (type) {
            case STATUS_MESSAGE:
                bar.setForeground(new Color(76, 76, 76));
                bar.setText(status + ";");
                break;
            case STATUS_SUCCESS:
                bar.setForeground(new Color(0, 150, 0));
                bar.setText(status + ";");
                break;
            case STATUS_ERROR:
                bar.setForeground(Color.red);
                bar.setText(status + ";");
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
        bar.setForeground(new Color(76, 76, 76));
        bar.setText("");
//        showStatusBar(false);
    }

    public void updateInfo() {
        info.setText("объектов: " + recordArrayList.size());
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

        progressBar.setValue(0);

        ArrayList<Record> foundRecords = new ArrayList<>(recordArrayList.size());

        int total = recordArrayList.size();
        int current = 0;

        for (Record record : recordArrayList) {
            current++;
            if (record.getSite().contains(text) || record.getLogin().contains(text)) {
                foundRecords.add(record);
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

        isEditableIcon.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                editModeJRadioButtonMenuItem.doClick();
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });

        controlPanelMouseListener = new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {
                mouse = e.getPoint();
                getComponentAt(mouse);
            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        };
        controlPanelMouseMotionAdapter = new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {

                // get location of Window
                int thisX = getLocation().x;
                int thisY = getLocation().y;

                // Determine how much the mouse moved since the initial click
                int xMoved = (thisX + e.getX()) - (thisX + mouse.x);
                int yMoved = (thisY + e.getY()) - (thisY + mouse.y);

                // Move window to this position
                int X = thisX + xMoved;
                int Y = thisY + yMoved;
                setLocation(X, Y);
            }
        };

        if (IS_MAC) {
            controlPanel.addMouseListener(controlPanelMouseListener);
            controlPanel.addMouseMotionListener(controlPanelMouseMotionAdapter);
            searchPanel.addMouseListener(controlPanelMouseListener);
            searchPanel.addMouseMotionListener(controlPanelMouseMotionAdapter);
        }
    }

    private void initStatusBar() {
//        bar.setFont(new Font("LucidaGrande", Font.PLAIN, 10));
//        bar.setForeground(new Color(76,76,76));
//        bar.setVerticalAlignment(JLabel.TOP);
        /*rowCount.setFont(new Font("Menlo", Font.PLAIN, 10));
        rowCount.setVerticalAlignment(JLabel.TOP);*/
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
                //showStatusBar(true);
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
                        if (MainForm.this.table.getSelectedColumn() == SITE_COLUMN_INDEX) {
                            String site = (String) MainForm.this.table.getModel()
                                    .getValueAt(row, SITE_COLUMN_INDEX);
                            StringUtils.openWebPage(site);
                        } else {
                            String copy = (String) MainForm.this.table.getModel().getValueAt(row,
                                    PASSWORD_COLUMN_INDEX);
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
                    /*if (e.getModifiers() > 0) {
                        int key = e.getKeyCode();
                        if (key == KeyEvent.VK_META) {
                            table.getCellEditor().cancelCellEditing();
                        }
                    }*/
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
                                    PASSWORD_COLUMN_INDEX);
                            copyToClipboard(copy);
                        }
                    }

                    if ((e.getKeyCode() == KeyEvent.VK_M) && ((e.getModifiers() & kEvent) != 0)) {
                        setState(JFrame.ICONIFIED);
                    }

                    //TODO FIX IF NEEDED
                    int key = e.getKeyCode();

                    if ((((key >= 65) && (key <= 90)) || ((key >= 97) && (key <= 122)) || ((key >= 48) && (key <= 57))) && e.getModifiers() <= 0) {
                        searchField.requestFocus();
                        searchField.setText(e.getKeyChar() + "");
                        searchField.setCaret(new DefaultCaret());
                        searchField.setCaretPosition(searchField.getText().length());
                        isSearchMode = true;
                    }

                    if (key == KeyEvent.VK_ESCAPE && isSearchMode) {
                        searchField.requestFocus();
                    }

                    if (key == KeyEvent.VK_HOME && table.getRowCount() > 0) {
                        table.setRowSelectionInterval(0, 0);
                    }

                    if (key == KeyEvent.VK_END && table.getRowCount() > 0) {
                        int index = table.getRowCount() - 1;
                        table.setRowSelectionInterval(index, index);
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
                int row = table.getSelectedRow();
                int col = table.getSelectedColumn();
                refreshLockTimer();
                if (row > -1) {
                    if (!isSearchMode) {
                        moveUpButton.setEnabled(hasPrevious());
                        moveDownButton.setEnabled(hasNext());
                    }
                }
                if (col == 0) {
                    table.setRowSelectionInterval(row, row);
                    table.setColumnSelectionInterval(col + 1, col + 1);
                }
            }
        });

        /*table.setCellSelectionEnabled(true); //test
        final ListSelectionModel listSelectionModel = table.getColumnModel().getSelectionModel();
        listSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listSelectionModel.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                //3->0->1 (calling 0) -> 3
                System.out.println("PP.>>>" + e.getFirstIndex() + " " + e.getLastIndex() + " " + e.getValueIsAdjusting());

                if (e.getFirstIndex() == 0) {
                    int pwd = PASSWORD_COLUMN_INDEX;
                    int sit = SITE_COLUMN_INDEX;
                    int rows = table.getRowCount();
                    int selected = table.getSelectedRow();
                    int lastIndex = e.getLastIndex();

                    if (lastIndex != 0) {
                        if (lastIndex == pwd) {
                            if (rows > selected + 1) {
                                table.setRowSelectionInterval(selected + 1, selected + 1);
                            } else {
                                table.setRowSelectionInterval(0, 0);
                            }
                            table.setColumnSelectionInterval(2, 2);
                            table.setColumnSelectionInterval(1, 1);
                        } else if (lastIndex == sit) {
                            if (selected - 1 >= 0) {
                                table.setRowSelectionInterval(selected - 1, selected - 1);
                            } else {
                                table.setRowSelectionInterval(rows - 1, rows - 1);
                            }
                            table.setColumnSelectionInterval(2, 2);
                            table.setColumnSelectionInterval(pwd, pwd);

                        }

                    } else {
                        table.setRowSelectionInterval(selected - 1, selected - 1);
                    }
                }
            }
        });*/

        tableModelListener = new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                int row = e.getFirstRow();
                int col = e.getColumn();
                if (row > -1 && col > -1) {

                    String value = (String) table.getValueAt(row, col);

                    Record oldRec = recordArrayList.get(row);
                    Record newRec = new Record();
                    newRec.setSite(oldRec.getSite());
                    newRec.setLogin(oldRec.getLogin());
                    newRec.setPassword(oldRec.getPassword());

                    String prevValue = "";
                    if (col == SITE_COLUMN_INDEX) {
                        newRec.setSite(value);
                        prevValue = oldRec.getSite();
                    }
                    if (col == LOGIN_COLUMN_INDEX) {
                        newRec.setLogin(value);
                        prevValue = oldRec.getLogin();
                    }

                    if (col == PASSWORD_COLUMN_INDEX) {
                        newRec.setPassword(value);
                        prevValue = oldRec.getPassword();
                    }

                    recordArrayList.set(row, newRec);

                    if (isFirstLaunch) {
                        setEdited(false);
                    } else {
                        setEdited(true);
                    }
//                    if (table.getEditingRow() > -1 && table.getEditingColumn() > -1) {
                    if (!history.isHistoryCall()) {
                        history.register(new ChangeCellValueAction(new Point(row, col), prevValue, value));
                    }
                }
            }
        };

        table.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (!isSearchMode) {
                    setControlButtonsEnabled(true);
                    moveUpButton.setEnabled(hasPrevious());
                    moveDownButton.setEnabled(hasNext());
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

    private void initScrollPaneListeners() {
        scrollPane.getVerticalScrollBar().setUnitIncrement(5);
        scrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                refreshLockTimer();
            }
        });
    }

    private void initMenu() {
        editModeJRadioButtonMenuItem = new JRadioButtonMenuItem();

        openItem.setAccelerator(getAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.META_MASK),
                KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK)));

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
                    initHistory();
                    moveUpButton.setEnabled(false);
                    moveDownButton.setEnabled(false);

                    setStatus("Количество записей: " + table.getModel().getRowCount(), STATUS_MESSAGE);
                }
            }
        });
        fileJMenu.add(openItem);

        saveItem.setAccelerator(getAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.META_MASK),
                KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK)));
        saveItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isSearchMode) {
                    saveStorage();
                }
            }
        });
        fileJMenu.add(saveItem);

        blockItem.setAccelerator(getAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.META_MASK),
                KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_MASK)));
        blockItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (PasswordStorrager.isUnlocked) {
                    PasswordStorrager.isUnlocked = false;
                    setVisible(false);
                    new AuthorizeDialog();
                }
            }
        });

        fileJMenu.add(blockItem);

        settingsItem.setAccelerator(getAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, InputEvent.META_MASK),
                KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, InputEvent.CTRL_MASK)));

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

        jMenuBar1.add(fileJMenu);

        undoItem.setAccelerator(getAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.META_MASK),
                KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK)));
        undoItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isSearchMode) {
                    history.undo();
                }
            }
        });
        editJMenu.add(undoItem);

        redoItem.setAccelerator(getAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.META_DOWN_MASK | InputEvent.SHIFT_MASK),
                KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK)));
        redoItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isSearchMode) {
                    history.redo();
                }
            }
        });
        editJMenu.add(redoItem);

        editJMenu.add(new JSeparator());

        editModeJRadioButtonMenuItem.setAccelerator(getAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.META_MASK),
                KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK)));

        editModeJRadioButtonMenuItem.setSelected(false);
        editModeJRadioButtonMenuItem.setText("Режим редактирования");
        editModeJRadioButtonMenuItem.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {

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

        editJMenu.add(editModeJRadioButtonMenuItem);
        editJMenu.add(new JSeparator());

        addItem.setAccelerator(getAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.META_MASK),
                KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK)));
        addItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isSearchMode) {
                    addNewRecord(recordArrayList.size(), 1);
                    if (!editModeJRadioButtonMenuItem.isSelected()) {
                        editModeJRadioButtonMenuItem.doClick();
                    }
                }
            }
        });
        editJMenu.add(addItem);

        addSomeItem.setAccelerator(getAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.META_MASK | InputEvent.SHIFT_MASK),
                KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK)));
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
                                    if (row >= 0) {
                                        addNewRecord(row + 1, count); //after selection //row = before
                                    } else {
                                        addNewRecord(table.getRowCount(), count); //after selection //row = before
                                    }
                                }
                                if (!editModeJRadioButtonMenuItem.isSelected()) {
                                    editModeJRadioButtonMenuItem.doClick();
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
        editJMenu.add(new JSeparator());

        addUpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.ALT_MASK | InputEvent.SHIFT_MASK));
        addUpItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addUpButton.getActionListeners()[0].actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
            }
        });
        editJMenu.add(addUpItem);

        addDownItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.ALT_MASK | InputEvent.SHIFT_MASK));
        addDownItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addDownButton.getActionListeners()[0].actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
            }
        });
        editJMenu.add(addDownItem);

        moveUpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.CTRL_MASK | InputEvent.ALT_MASK | InputEvent.SHIFT_MASK));
        moveUpItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moveUpButton.getActionListeners()[0].actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
            }
        });
        editJMenu.add(moveUpItem);

        moveDownItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.CTRL_MASK | InputEvent.ALT_MASK | InputEvent.SHIFT_MASK));
        moveDownItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moveDownButton.getActionListeners()[0].actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
            }
        });
        editJMenu.add(moveDownItem);
        editJMenu.add(new JSeparator());

        searchMenuItem.setAccelerator(getAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.META_MASK),
                KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK)));
        searchMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (editModeJRadioButtonMenuItem.isSelected()) {
                    editModeJRadioButtonMenuItem.setSelected(false);
                }
                searchField.requestFocus();
            }
        });
        editJMenu.add(searchMenuItem);

        jMenuBar1.add(editJMenu);

        copySiteItem.setAccelerator(getAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, InputEvent.META_MASK),
                KeyStroke.getKeyStroke(KeyEvent.VK_1, InputEvent.CTRL_MASK)));
        copySiteItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copySelectedCell(SITE_COLUMN_INDEX);
            }
        });
        copyJMenu.add(copySiteItem);

        copyLoginItem.setAccelerator(getAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, InputEvent.META_MASK),
                KeyStroke.getKeyStroke(KeyEvent.VK_2, InputEvent.CTRL_MASK)));
        copyLoginItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copySelectedCell(LOGIN_COLUMN_INDEX);
            }
        });
        copyJMenu.add(copyLoginItem);

        copyPasswordItem.setAccelerator(getAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, InputEvent.META_MASK),
                KeyStroke.getKeyStroke(KeyEvent.VK_3, InputEvent.CTRL_MASK)));
        copyPasswordItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copySelectedCell(PASSWORD_COLUMN_INDEX);
            }
        });
        copyJMenu.add(copyPasswordItem);

        jMenuBar1.add(copyJMenu);

        aboutItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new AboutApplication();
            }
        });
        referenceMenu.add(aboutItem);

        if (IS_WINDOWS) {
            jMenuBar1.add(referenceMenu);
        }

        setJMenuBar(jMenuBar1);
    }

    private void initHistory() {
        history = new History(this);
        refreshLockTimer();
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
                String copy = (String) table.getModel().getValueAt(table.getSelectedRow(), SITE_COLUMN_INDEX);
                copyToClipboard(copy);
            }
        });
        menuItemCopyLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (table.getSelectedRow() != -1) {
                    String copy = (String) table.getModel().getValueAt(table.getSelectedRow(), LOGIN_COLUMN_INDEX);
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
        int preferredWidth = tableColumn.getMinWidth();
        int maxWidth = tableColumn.getMaxWidth();
        for (int row = 0; row < table.getRowCount(); row++) {
            TableCellRenderer cellRenderer = table.getCellRenderer(row, NUMBER_COLUMN_INDEX);
            Component c = table.prepareRenderer(cellRenderer, row, NUMBER_COLUMN_INDEX);
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
//        setStatus(bar.getText(), STATUS_MESSAGE);
        table.setBorder(BorderFactory.createEmptyBorder());
        table.setModel(createTableModel(recordArrayList));
        table.setRowHeight(20);
        table.setFont(new Font("LucidaGrande", Font.PLAIN, 12));

        initTable();

        table.getColumn(NUMBER_COLUMN_NAME).setCellEditor(new TableEditor(new JTextField(NUMBER_COLUMN_NAME), this));
        table.getColumn(SITE_COLUMN_NAME).setCellEditor(new TableEditor(new JTextField(SITE_COLUMN_NAME), this));
        table.getColumn(LOGIN_COLUMN_NAME).setCellEditor(new TableEditor(new JTextField(LOGIN_COLUMN_NAME), this));
        table.getColumn(PASSWORD_COLUMN_NAME).setCellEditor(new TableEditor(new JTextField(PASSWORD_COLUMN_NAME), this));

        resizeTableColumns(table.getColumn(NUMBER_COLUMN_NAME));

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setDragEnabled(false);
        table.getTableHeader().setReorderingAllowed(false); //to prevent column dragging(moving)

        table.setComponentPopupMenu(popupMenu);
        table.setSurrendersFocusOnKeystroke(true);
        table.getModel().addTableModelListener(tableModelListener);
        updateInfo();
    }

    public void saveStorage() {
        int rows = table.getRowCount();
        System.out.println("rows to save:" + rows);

        editModeJRadioButtonMenuItem.setSelected(false);

        recordArrayList = new ArrayList<>(rows);
        for (int i = 0; i < rows; i++) {
            Record record = new Record();
            record.setSite((String) table.getModel().getValueAt(i, SITE_COLUMN_INDEX));
            record.setLogin((String) table.getModel().getValueAt(i, LOGIN_COLUMN_INDEX));
            record.setPassword((String) table.getModel().getValueAt(i, PASSWORD_COLUMN_INDEX));
            recordArrayList.add(record);
        }
        new XmlParser().saveRecords(recordArrayList);
        loadList(recordArrayList);
        setEdited(false);
        history.save();
        setStatus("Сохранено.", STATUS_SUCCESS);
    }

    public void addNewRecord(int index, int count) {
        /*if (!editModeJRadioButtonMenuItem.isSelected()) {
            editModeJRadioButtonMenuItem.doClick();
        }*/

        isEditableIcon.setIcon(new ImageIcon(getClass().getResource("/icons/controls/unlock.png")));

        if (count > 100) {
            Record[] tmp = new Record[recordArrayList.size()];
            tmp = recordArrayList.toArray(tmp);

            Record[] rec1 = Arrays.copyOfRange(tmp, 0, index);

            Record[] rec2 = new Record[count];
            for (int i = 0; i < count; i++) {
                rec2[i] = new Record();
            }
            Record[] rec3 = Arrays.copyOfRange(tmp, index, tmp.length);
            Record[] rec;
            rec = concatenate(rec1, rec2);
            rec = concatenate(rec, rec3);

            recordArrayList = new ArrayList<>(Arrays.asList(rec));
        } else {
            for (int i = 0; i < count; i++) {
                recordArrayList.add(index, new Record());
            }
        }
        loadList(recordArrayList);

        table.clearSelection();
        table.setRowSelectionInterval(index, index);
        //scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
        scrollToVisible(table, index, 1);
        table.requestFocus();
        setEdited(true);
        if (!history.isHistoryCall()) {
            history.register(new AddRowAction(index, count));
            if (count == 1) {
                setStatus("Добавлена запись", STATUS_MESSAGE);
            } else {
                setStatus("Добавлено записей: " + count, STATUS_MESSAGE);
            }
        }
    }

    public void exchangeRecords(int index1, int index2) {
        Record rec1 = recordArrayList.get(index1);
        Record rec2 = recordArrayList.get(index2);
        recordArrayList.set(index1, rec2);
        recordArrayList.set(index2, rec1);
        loadList(recordArrayList);
        table.clearSelection();
        table.setRowSelectionInterval(index1, index1);
        if (!history.isHistoryCall()) {
            history.register(new ExchangedRowsAction(index1, index2));
        }
        setEdited(true);
    }

    public void deleteSelectedRecords(int index1, int index2) {
//index1 - меньше index2
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

        ArrayList<Record> removedRecords = new ArrayList<>();

        if (recordArrayList.size() > 0) {
            int diff = index2 - index1;
            removedRecords.add(recordArrayList.get(index1));
            recordArrayList.remove(index1);
            for (int i = 0; i < diff; i++) {
                removedRecords.add(recordArrayList.get(index1));
                recordArrayList.remove(index1);
            }
            loadList(recordArrayList);
            table.clearSelection();

//            editModeJRadioButtonMenuItem.setSelected(true);
            isEditableIcon.setIcon(new ImageIcon(getClass().getResource("/icons/controls/unlock.png")));

            if (index1 >= 0) {
                if (index1 <= recordArrayList.size()) {
                    if (index1 == recordArrayList.size()) {
                        if (index1 != 0) {
                            table.setRowSelectionInterval(index1 - 1, index1 - 1);
                            scrollToVisible(table, index1 - 1, 1);
                        }
                    } else {
                        table.setRowSelectionInterval(index1, index1);
                        scrollToVisible(table, index1, 1);
                    }
                }
            }

            if (!history.isHistoryCall()) {
                history.register(new RemoveRowAction(index1, removedRecords));
                if (index1 != index2) {
                    setStatus("Удалено записей: " + (index2 + 1 - index1), STATUS_MESSAGE);
                } else {
                    setStatus("Удалена запись", STATUS_MESSAGE);
                }
            }
        }

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
        if (IS_MAC) {
            setTitle(file.getName());
        } else {
            setTitle(APPLICATION_NAME);
        }
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
        }

        tableModel.addColumn(NUMBER_COLUMN_NAME, number);
        tableModel.addColumn(SITE_COLUMN_NAME, siteData);
        tableModel.addColumn(LOGIN_COLUMN_NAME, loginData);
        tableModel.addColumn(PASSWORD_COLUMN_NAME, pwdData);
        return tableModel;
    }

    class TableEditor extends DefaultCellEditor {

        boolean isAutoComplete = false;

        JTextField textField;

        MainForm mainForm;

        int col = -1;

        public TableEditor(JTextField textField, MainForm mainForm) {
            super(textField);
            this.textField = textField;
            this.mainForm = mainForm;
            initChangeListener();
        }

        private void initChangeListener() {
            textField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    if (!isAutoComplete) {
                        autoComplete();
                    } else {
                        isAutoComplete = false;
                    }
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                }

                @Override
                public void changedUpdate(DocumentEvent e) {

                }
            });
        }

        private void autoComplete() {
            System.out.println("autoComplete");
            ArrayList<Record> records = mainForm.recordArrayList;
            final String text = textField.getText();
            final int start = text.length();

            if (col == mainForm.SITE_COLUMN_INDEX ||
                    col == mainForm.LOGIN_COLUMN_INDEX) {
                ArrayList<String> sites = new ArrayList<>(records.size());
                ArrayList<String> logins = new ArrayList<>(records.size());
                for (Record record : records) {
                    sites.add(record.getSite());
                    logins.add(record.getLogin());
                }
                Comparator<String> comparator = new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                        return ((o1.length() > o2.length()) ? 1 : (o1.length() < o2.length()) ? -1 : 0);
                    }
                };

                Collections.sort(sites, comparator);
                Collections.sort(logins, comparator);

                final ArrayList<String> result = new ArrayList<>();

                if (col == mainForm.SITE_COLUMN_INDEX) {
                    for (String site : sites) {
                        if (site.startsWith(text) && site.length() > text.length()) {
                            result.add(site);
                        }
                    }
                }
                if (col == mainForm.LOGIN_COLUMN_INDEX) {
                    for (String login : logins) {
                        if (login.startsWith(text) && login.length() > text.length()) {
                            result.add(login);
                        }
                    }
                }
                Collections.sort(result);

                if (result.size() > 0) {
                    if (result.get(0).length() > start) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                isAutoComplete = true;
                                textField.setText(result.get(0));
                                textField.setCaretPosition(result.get(0).length());
                                textField.moveCaretPosition(text.length());
                                isAutoComplete = false;
                            }
                        });
                    }
                }
            }
        }

        @Override
        public boolean isCellEditable(EventObject anEvent) {

            if (anEvent instanceof KeyEvent) {
                return startWithKeyEvent((KeyEvent) anEvent) && isNumberCell();
            }
            return isNumberCell();
        }

        private boolean isNumberCell() {
            return textField != null && !textField.getText().equals(NUMBER_COLUMN_NAME)
                    && editModeJRadioButtonMenuItem.isSelected();
        }

        private boolean startWithKeyEvent(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_META) {
                return false;
            }
            // check modifiers as needed, this here is just a quick example ;-)
            /*if ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0) {
                return false;
            }*/
            // check for a list of function key strokes
            /*if (excludes.contains(KeyStroke.getKeyStrokeForEvent(e)) {
                return false;
            }*/
            return true;
        }

        @Override
        public void addCellEditorListener(CellEditorListener l) {
            textField.setFont(new Font("LucidaGrande", Font.PLAIN, 12));
            textField.setCaret(new DefaultCaret());
            textField.setCaretPosition(textField.getText().length());
            MainForm mf = (MainForm) findWindow(textField);
            mf.undoItem.setEnabled(false);
            mf.redoItem.setEnabled(false);

            int editingColumn = mf.table.getEditingColumn();

            if (editingColumn == mf.SITE_COLUMN_INDEX) {
                col = editingColumn;
            } else if (editingColumn == mf.LOGIN_COLUMN_INDEX) {
                col = editingColumn;
            } else {
                col = -1;
            }

            super.addCellEditorListener(l);
        }

        @Override
        public boolean stopCellEditing() {
            MainForm mf = (MainForm) findWindow(textField);
            mf.undoItem.setEnabled(true);
            mf.redoItem.setEnabled(true);
            return super.stopCellEditing();
        }

        @Override
        public void cancelCellEditing() {
            MainForm mf = (MainForm) findWindow(textField);
            mf.undoItem.setEnabled(true);
            mf.redoItem.setEnabled(true);
            super.cancelCellEditing();
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

    boolean hasNext() {
        return table.getSelectedRow() >= 0 && table.getSelectedRow() < table.getRowCount() - 1;
    }

    boolean hasPrevious() {
        return table.getSelectedRow() >= 0 && table.getSelectedRow() > 0;
    }

    public void setValue(int index, Record record) {
        table.setValueAt(record.getSite(), index, SITE_COLUMN_INDEX);
        table.setValueAt(record.getLogin(), index, LOGIN_COLUMN_INDEX);
        table.setValueAt(record.getPassword(), index, PASSWORD_COLUMN_INDEX);
    }

    public void refreshLockTimer() {
        lockTimer.restart();
    }

    public static void scrollToVisible(JTable table, int rowIndex, int vColIndex) {
        if (!(table.getParent() instanceof JViewport)) return;
        JViewport viewport = (JViewport) table.getParent();
        Rectangle rect = table.getCellRect(rowIndex, vColIndex, true);
        Point pt = viewport.getViewPosition();
        rect.setLocation(rect.x - pt.x, rect.y - pt.y);
        viewport.scrollRectToVisible(rect);
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

class MovingTogether extends ComponentAdapter {
    private Window window, dialog;

    public MovingTogether(JFrame window, JDialog dialog) {
        this.window = window;
        this.dialog = dialog;
        if (window.getComponentListeners().length > 1) {
            window.removeComponentListener(this);
        }
        window.addComponentListener(this);
    }

    public void componentMoved(ComponentEvent e) {
        Window win = (Window) e.getComponent();
        Dimension size = dialog.getSize();
        if (win == window && dialog.isVisible()) {
            Point location = window.getLocation();
            Dimension dim = window.getSize();
            int centerWidth = location.x + dim.width / 2;
            centerWidth = centerWidth - size.width / 2;
            int height = location.y + 22;
            dialog.setLocation(centerWidth, height);

        }
    }

}
