package edu.passwordStorrager.gui;

import edu.passwordStorrager.core.Application;
import edu.passwordStorrager.core.Core;
import edu.passwordStorrager.core.PasswordStorrager;
import edu.passwordStorrager.core.PropertiesManager;
import edu.passwordStorrager.gui.elements.ControlButton;
import edu.passwordStorrager.gui.elements.ZebraJTable;
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
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;
import java.util.List;

import static edu.passwordStorrager.core.Application.*;
import static edu.passwordStorrager.utils.FrameUtils.*;

public class MainForm extends JFrame {

    public static final int STATUS_MESSAGE = 1, STATUS_ERROR = -1, STATUS_SUCCESS = 2;
    static final String SITE_COLUMN_NAME = "Сайт",
            LOGIN_COLUMN_NAME = "Логин", PASSWORD_COLUMN_NAME = "Пароль";
    private static final Logger log = Logger.getLogger(getCurrentClassName());
    private static final int COLUMN_MINIMUM_WIDTH = 120;
    private static final int COLUMN_COUNT = 3;
    private static final int SEARCH_MODE_NORMAL = 0;
    private static final int SEARCH_MODE_ALL = 1;
    private static final int SEARCH_MODE_SITE = 2;
    private static final int SEARCH_MODE_LOGIN = 3;
    private static final int SEARCH_MODE_PASSWORD = 4;
    public static boolean isFirstLaunch = true;
    static Timer timer;
    static int SITE_COLUMN_INDEX = 0;
    static int LOGIN_COLUMN_INDEX = 1;
    static int PASSWORD_COLUMN_INDEX = 2;
    private static Timer lockTimer; //for multiple windows
    private static TableModelListener tableModelListener;
    public ArrayList<Record> recordArrayList = new ArrayList<>();
    public JMenuBar jMenuBar1 = new JMenuBar();
    public JMenuItem undoItem = new JMenuItem("Отменить");
    public JMenuItem redoItem = new JMenuItem("Повторить");
    public ZebraJTable table;
    public JPanel controlPanel;
    protected JRadioButtonMenuItem editModeJRadioButtonMenuItem = new JRadioButtonMenuItem();
    SaveOnExitDialog saveOnExitDialog;
    private History history;
    private boolean isEdited = false;
    private JPopupMenu popupMenu;
    private JMenu fileJMenu = new JMenu("Файл");
    private JMenuItem openItem = new JMenuItem("Открыть");
    private JMenuItem saveItem = new JMenuItem("Сохранить");
    private JMenuItem blockItem = new JMenuItem("Блокировать");
    private JMenuItem settingsItem = new JMenuItem("Настройки");
    private JMenu editJMenu = new JMenu("Правка");
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
    private JPanel statusPanel;
    private JTextField searchField;
    private JButton moveUpButton;
    private JButton moveDownButton;
    private JButton addUpButton;
    private JButton addDownButton;
    private Point mouse = new Point(0, 0);
    private MouseListener controlPanelMouseListener;
    private MouseMotionAdapter controlPanelMouseMotionAdapter;
    private JProgressBar progressBar;
    private JLabel messageInfo;
    private JLabel messageStatus;
    private JLabel isEditableIcon;
    private JPanel searchPanel;
    private JLabel info;
    private Timer searchTimer;
    private boolean isSearchMode = false;
    private int currentSearchMode = SEARCH_MODE_NORMAL;
    private int disposeCounter = 0;


    public MainForm(ArrayList<Record> recordArrayList) {
        this.recordArrayList = recordArrayList;

        initComponents();

        requestFocus();
        table.requestFocus();

        initHistory();

        resetStatus();

        FrameUtils.registerWindow(this);
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

    public static void refreshLockTimer() {
        lockTimer.restart();
    }

    public static void stopLockTimer() {
        lockTimer.stop();
    }

    public static void scrollToVisible(JTable table, int rowIndex, int vColIndex) {
        if (!(table.getParent() instanceof JViewport)) return;
        JViewport viewport = (JViewport) table.getParent();
        Rectangle rect = table.getCellRect(rowIndex, vColIndex, true);
        Point pt = viewport.getViewPosition();
        rect.setLocation(rect.x - pt.x, rect.y - pt.y);
        viewport.scrollRectToVisible(rect);
    }

    private void initComponents() {
        initWindow();

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

        saveOnExitDialog = new SaveOnExitDialog(this);
    }

    private void initWindow() {
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
        setMinimumSize(new Dimension((COLUMN_COUNT * COLUMN_MINIMUM_WIDTH) + 20, 300));
        setPreferredSize(getFrameSize(getCurrentClassName()));
        setLocation(getFrameLocation(getCurrentClassName()));

        getRootPane().putClientProperty("apple.awt.brushMetalLook", Boolean.TRUE);
    }

    private void initWindowListeners() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
                FrameUtils.setFrameLocation(getClass().getEnclosingClass().getName(), getLocation());
                FrameUtils.setFrameSize(getClass().getEnclosingClass().getName(), getSize());
            }
        });

        addWindowFocusListener(new WindowFocusListener() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                refreshLockTimer();
                scrollPane.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(149, 149, 149)));
                scrollPane.invalidate();
                getContentPane().validate();
            }

            @Override
            public void windowLostFocus(WindowEvent e) {
                refreshLockTimer();
                scrollPane.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(177, 177, 177)));
                scrollPane.invalidate();
                getContentPane().validate();
            }
        });

        addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                scrollPane.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(149, 149, 149)));
                scrollPane.invalidate();
                getContentPane().validate();
                if (searchField.isFocusOwner()) {
                    updateSearchFieldSize();
                }
            }
        });

        getRootPane().registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        }, getKeyStrokeForOS(KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.META_MASK),
                KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_MASK)), JComponent.WHEN_IN_FOCUSED_WINDOW);

        addMouseListener(controlPanelMouseListener);
        addMouseMotionListener(controlPanelMouseMotionAdapter);
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

    private void initTable() {

        TableColumn site = table.getColumnModel().getColumn(0);
        site.setHeaderValue(SITE_COLUMN_NAME);
        site.setMinWidth(COLUMN_MINIMUM_WIDTH);
        site.setWidth(150);
        site.setResizable(true);

        TableColumn login = table.getColumnModel().getColumn(1);
        login.setHeaderValue(LOGIN_COLUMN_NAME);
        login.setMinWidth(COLUMN_MINIMUM_WIDTH);
        login.setWidth(150);
        login.setResizable(true);

        TableColumn password = table.getColumnModel().getColumn(2);
        password.setHeaderValue(PASSWORD_COLUMN_NAME);
        password.setMinWidth(COLUMN_MINIMUM_WIDTH);
        password.setResizable(true);

        SITE_COLUMN_INDEX = table.getColumn(SITE_COLUMN_NAME).getModelIndex();
        LOGIN_COLUMN_INDEX = table.getColumn(LOGIN_COLUMN_NAME).getModelIndex();
        PASSWORD_COLUMN_INDEX = table.getColumn(PASSWORD_COLUMN_NAME).getModelIndex();

        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 22));
        header.setBackground(new Color(246, 246, 246));
        header.setDefaultRenderer(new MainFormTableHeader());

        JPanel corner = new JPanel();
        corner.setBackground(new Color(246, 246, 246));
        corner.setBorder(new TableHeaderBorder(corner.getSize(), new Color(246, 246, 246)));
        scrollPane.setCorner(ScrollPaneConstants.UPPER_RIGHT_CORNER, corner); //square, квадрат между table и scrollpane
    }

    private void initSearchBarListeners() {

        searchField.putClientProperty("JTextField.variant", "search");

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            void search() {
                if (!searchField.getText().isEmpty()) {
                    startSearchTimer(searchField.getText());
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

            @Override
            public void focusGained(FocusEvent e) {
                isSearchMode = true;
                updateSearchFieldSize();
                refreshLockTimer();
            }

            @Override
            public void focusLost(FocusEvent e) {

                updateSearchFieldSize();
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
                    setControlsEnabled(true);
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

        initSearchTimer("");

        searchField.registerKeyboardAction(clearSearchFieldAction, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_FOCUSED);
        if (IS_MAC) {
            searchField.registerKeyboardAction(clearSearchFieldAction, KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE,
                    InputEvent.META_MASK), JComponent.WHEN_FOCUSED);
        } else if (IS_WINDOWS) {
            searchField.registerKeyboardAction(clearSearchFieldAction, KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE,
                    InputEvent.CTRL_MASK), JComponent.WHEN_FOCUSED);
        }

        ButtonGroup group = new ButtonGroup();
        JPopupMenu popup = new JPopupMenu("Режим поиска:");

        JMenuItem normal = new JRadioButtonMenuItem("обычный");
        normal.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentSearchMode = SEARCH_MODE_NORMAL;
                refreshLockTimer();
                searchTimer.restart();
            }
        });
        JMenuItem all = new JRadioButtonMenuItem("по всему");
        all.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentSearchMode = SEARCH_MODE_ALL;
                refreshLockTimer();
                searchTimer.restart();
            }
        });
        JMenuItem site = new JRadioButtonMenuItem("по сайтам");
        site.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentSearchMode = SEARCH_MODE_SITE;
                refreshLockTimer();
                searchTimer.restart();
            }
        });
        JMenuItem login = new JRadioButtonMenuItem("по логинам");
        login.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentSearchMode = SEARCH_MODE_LOGIN;
                refreshLockTimer();
                searchTimer.restart();
            }
        });
        JMenuItem password = new JRadioButtonMenuItem("по паролям");
        password.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentSearchMode = SEARCH_MODE_PASSWORD;
                refreshLockTimer();
                searchTimer.restart();
            }
        });

        popup.add(normal);
        group.add(normal);
        popup.add(all);
        group.add(all);
        popup.add(new JSeparator());
        popup.add(site);
        group.add(site);
        popup.add(login);
        group.add(login);
        popup.add(password);
        group.add(password);

        normal.setSelected(true);

        searchField.setComponentPopupMenu(popup);
    }

    private void updateSearchFieldSize() {
        final int windowThreshold = 580;
        int windowWidth = getSize().width;

        int width;

        if (searchField.isFocusOwner()) {
            if (windowWidth > windowThreshold) {
                width = 300;
            } else {
                width = 300-(windowThreshold-windowWidth);
            }
        } else {
            if (windowWidth > windowThreshold) {
                width = 150;
            } else {
                width = 150 - (windowThreshold - windowWidth);
            }
        }

        int height = searchField.getHeight();
        searchField.setMinimumSize(new Dimension(width, height));
        searchField.setMaximumSize(new Dimension(width, height));
        searchField.invalidate();
        controlPanel.validate();
    }

    private void startSearchTimer(final String text) {
        if (searchTimer != null) {
            searchTimer.stop();
        }
        initSearchTimer(text);
        searchTimer.setRepeats(false);
        searchTimer.start();
    }

    private void initSearchTimer(final String text) {
        searchTimer = new Timer(500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isSearchMode = true;
                editModeJRadioButtonMenuItem.setEnabled(false);

                progressBar.setVisible(true);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        loadList(searchRecord(text));
                        setStatus("Найдено записей: " + table.getRowCount(), STATUS_MESSAGE);
                    }
                }).start();
                setControlsEnabled(false);

            }
        });
    }

    public void setStatus(String status, int type) {
        switch (type) {
            case STATUS_MESSAGE:
                messageInfo.setForeground(new Color(76, 76, 76));
                messageInfo.setText(status + ";");
                break;
            case STATUS_SUCCESS:
                messageStatus.setForeground(new Color(0, 150, 0));
                messageStatus.setText(status + ";");
                break;
            case STATUS_ERROR:
                messageStatus.setForeground(Color.red);
                messageStatus.setText(status + "!");
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
        messageInfo.setForeground(new Color(76, 76, 76));
        messageInfo.setText("");
        messageStatus.setForeground(new Color(76, 76, 76));
        messageStatus.setText("");

//        showStatusBar(false);
    }

    public void updateInfo() {
        info.setText("объектов: " + recordArrayList.size());
    }

    private void setControlsEnabled(boolean value) {
        /*addItem.setEnabled(value);
        addSomeItem.setEnabled(value);
        deleteItem.setEnabled(value);
        deleteSomeItem.setEnabled(value);*/

        addUpItem.setEnabled(value);
        addDownItem.setEnabled(value);
        moveUpItem.setEnabled(value);
        moveDownItem.setEnabled(value);

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

            switch (currentSearchMode) {
                case SEARCH_MODE_NORMAL:
                    if (record.getSite().contains(text) || record.getLogin().contains(text)) {
                        foundRecords.add(record);
                    }
                    break;
                case SEARCH_MODE_ALL:
                    if (record.getSite().contains(text) || record.getLogin().contains(text)
                            || record.getPassword().contains(text)) {
                        foundRecords.add(record);
                    }
                    break;
                case SEARCH_MODE_SITE:
                    if (record.getSite().contains(text)) {
                        foundRecords.add(record);
                    }
                    break;
                case SEARCH_MODE_LOGIN:
                    if (record.getLogin().contains(text)) {
                        foundRecords.add(record);
                    }
                    break;
                case SEARCH_MODE_PASSWORD:
                    if (record.getPassword().contains(text)) {
                        foundRecords.add(record);
                    }
                    break;
            }
            progressBar.setValue((int) ((double) current / total) * 100);
        }
        return foundRecords;
    }

    private void initControlBar() {
        /*addUpButton.putClientProperty("JButton.buttonType", "gradient");
        addDownButton.putClientProperty("JButton.buttonType", "gradient");
        moveUpButton.putClientProperty("JButton.buttonType", "gradient");
        moveDownButton.putClientProperty("JButton.buttonType", "gradient");*/
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
            Image img = ImageIO.read(getClass().getResource("/icons/controls/AddUp.png"));
            addUpButton.setIcon(new ImageIcon(img));
            img = ImageIO.read(getClass().getResource("/icons/controls/AddDown.png"));
            addDownButton.setIcon(new ImageIcon(img));
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
                addUpItem.getActionListeners()[0].actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
            }
        });
        addDownButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addDownItem.getActionListeners()[0].actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));

            }
        });

        moveUpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moveDownItem.getActionListeners()[0].actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
            }
        });

        moveDownButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moveDownItem.getActionListeners()[0].actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
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

        isEditableIcon.setIcon(new ImageIcon(getClass().getResource("/icons/controls/lock.png")));
        isEditableIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                editModeJRadioButtonMenuItem.doClick();
            }
        });

        controlPanelMouseListener = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mouse = e.getPoint();
                getComponentAt(mouse);
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
//        messageInfo.setFont(new Font("LucidaGrande", Font.PLAIN, 10));
//        messageInfo.setForeground(new Color(76,76,76));
//        messageInfo.setVerticalAlignment(JLabel.TOP);
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
                //setStatus(messageInfo.getText(),STATUS_MESSAGE);
            }
        });
    }

    private void initTableListeners() {
        MouseListener copyMouseListener = new MouseAdapter() {
            int count = 0;
            Timer timer = new Timer(500, new ActionListener() {
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
                    if (table.getSelectedRow() >= 0) {
                        if (table.getSelectedColumn() == SITE_COLUMN_INDEX) {
                            String site = (String) table.getValueAt(row, SITE_COLUMN_INDEX);
                            StringUtils.openWebPage(site);
                        } else {
                            String copy = (String) table.getValueAt(row,
                                    PASSWORD_COLUMN_INDEX);
                            copyToClipboard(copy);
                        }
                        MainForm.refreshLockTimer();
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
                if (col == 0 && row >= 0) {
                    table.setRowSelectionInterval(row, row);
                    table.setColumnSelectionInterval(col + 1, col + 1);
                }
            }
        });

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
                        if (!prevValue.equals(value)) {
                            history.register(new ChangeCellValueAction(new Point(row, col), prevValue, value));
                        }
                    }
                }
            }
        };

        table.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (!isSearchMode) {
                    setControlsEnabled(true);
                    moveUpButton.setEnabled(hasPrevious());
                    moveDownButton.setEnabled(hasNext());
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                setControlsEnabled(false);
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

        openItem.setAccelerator(getKeyStrokeForOS(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.META_MASK),
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

        saveItem.setAccelerator(getKeyStrokeForOS(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.META_MASK),
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

        blockItem.setAccelerator(getKeyStrokeForOS(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.META_MASK),
                KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_MASK)));
        blockItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!saveOnExitDialog.isVisible()) {
                    if (PasswordStorrager.isUnlocked) {
                        PasswordStorrager.isUnlocked = false;
                        ArrayList<Window> windows = FrameUtils.getWindows(MainForm.class);
                        for (Window window : windows) {
                            window.setVisible(false);
                        }
                        new AuthorizeDialog(false);
                    }
                }
            }
        });

        fileJMenu.add(blockItem);

        settingsItem.setAccelerator(getKeyStrokeForOS(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, InputEvent.META_MASK),
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

        ////////////////////////////////////////////////////////////

        initHistoryGroupItems();

        ////--------------------------------------------------

        editJMenu.add(new JSeparator());

        initEditModeItem();

        ////--------------------------------------------------

        editJMenu.add(new JSeparator());

        initAddDeleteGroupItems();

        ////--------------------------------------------------

        editJMenu.add(new JSeparator());

        initAddMoveGroupItems();

        ////--------------------------------------------------

        editJMenu.add(new JSeparator());

        initSearchItem();

        ////////////////////////////////////////////////////////////

        initCopyMenuItems();

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

    private void initCopyMenuItems() {
        copySiteItem.setAccelerator(getKeyStrokeForOS(KeyStroke.getKeyStroke(KeyEvent.VK_1, InputEvent.META_MASK),
                KeyStroke.getKeyStroke(KeyEvent.VK_1, InputEvent.CTRL_MASK)));
        copySiteItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copySelectedCell(SITE_COLUMN_INDEX);
            }
        });
        copyJMenu.add(copySiteItem);

        copyLoginItem.setAccelerator(getKeyStrokeForOS(KeyStroke.getKeyStroke(KeyEvent.VK_2, InputEvent.META_MASK),
                KeyStroke.getKeyStroke(KeyEvent.VK_2, InputEvent.CTRL_MASK)));
        copyLoginItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copySelectedCell(LOGIN_COLUMN_INDEX);
            }
        });
        copyJMenu.add(copyLoginItem);

        copyPasswordItem.setAccelerator(getKeyStrokeForOS(KeyStroke.getKeyStroke(KeyEvent.VK_3, InputEvent.META_MASK),
                KeyStroke.getKeyStroke(KeyEvent.VK_3, InputEvent.CTRL_MASK)));
        copyPasswordItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copySelectedCell(PASSWORD_COLUMN_INDEX);
            }
        });
        copyJMenu.add(copyPasswordItem);

        jMenuBar1.add(copyJMenu);
    }

    private void initSearchItem() {
        searchMenuItem.setAccelerator(getKeyStrokeForOS(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.META_MASK),
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
    }

    private void initAddMoveGroupItems() {
        addUpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.ALT_MASK | InputEvent.SHIFT_MASK));
        addUpItem.addActionListener(new ActionListener() {
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

        addDownItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.ALT_MASK | InputEvent.SHIFT_MASK));
        addDownItem.addActionListener(new ActionListener() {
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

        moveUpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.CTRL_MASK | InputEvent.ALT_MASK | InputEvent.SHIFT_MASK));
        moveUpItem.addActionListener(new ActionListener() {
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

        moveDownItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.CTRL_MASK | InputEvent.ALT_MASK | InputEvent.SHIFT_MASK));
        moveDownItem.addActionListener(new ActionListener() {
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
        editJMenu.add(addUpItem);
        editJMenu.add(addDownItem);
        editJMenu.add(moveUpItem);
        editJMenu.add(moveDownItem);
    }

    private void initAddDeleteGroupItems() {
        addItem.setAccelerator(getKeyStrokeForOS(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.META_MASK),
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

        addSomeItem.setAccelerator(getKeyStrokeForOS(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.META_MASK | InputEvent.SHIFT_MASK),
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

        deleteItem.setAccelerator(getKeyStrokeForOS(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, InputEvent.META_MASK),
                KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, InputEvent.CTRL_MASK)));
        deleteItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isSearchMode) {
                    deleteSelectedRecords(table.getSelectedRow(), table.getSelectedRow());
                }
            }
        });

        deleteSomeItem.setAccelerator(getKeyStrokeForOS(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, InputEvent.META_MASK | InputEvent.SHIFT_MASK),
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
        editJMenu.add(addItem);
        editJMenu.add(addSomeItem);
        editJMenu.add(deleteItem);
        editJMenu.add(deleteSomeItem);
    }

    private void initEditModeItem() {
        editModeJRadioButtonMenuItem.setAccelerator(getKeyStrokeForOS(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.META_MASK),
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
    }

    private void initHistoryGroupItems() {
        undoItem.setAccelerator(getKeyStrokeForOS(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.META_MASK),
                KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK)));
        undoItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isSearchMode) {
                    history.undo();
                }
            }
        });

        redoItem.setAccelerator(getKeyStrokeForOS(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.META_DOWN_MASK | InputEvent.SHIFT_MASK),
                KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK)));
        redoItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isSearchMode) {
                    history.redo();
                }
            }
        });

        editJMenu.add(undoItem);
        editJMenu.add(redoItem);
    }

    private void initHistory() {
        history = new History(this);
        refreshLockTimer();
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
            if (!copy.isEmpty() && !copy.equals("")) {
                FrameUtils.copyToClipboard(copy);
                setStatus("Скопировано: " + copy, STATUS_SUCCESS);
            } else {
                setStatus("Нечего копировать", STATUS_ERROR);
            }
        } else {
            setStatus("Нечего копировать", STATUS_ERROR);
        }
    }

    private void copySelectedCell(int column) {
        if (table.getSelectedRow() >= 0) {
            String copy = (String) table.getValueAt(table.getSelectedRow(), column);
            copyToClipboard(copy);
        }
    }

    public void loadList(ArrayList<Record> recordArrayList) {
        //Record[] recordsList = recordArrayList.toArray(new Record[recordArrayList.size()]);
//        setStatus(messageInfo.getText(), STATUS_MESSAGE);
        table.setBorder(BorderFactory.createEmptyBorder());//not to draw focus for table
        table.setModel(createTableModel(recordArrayList));
        table.setRowHeight(20);
        table.setFont(new Font("LucidaGrande", Font.PLAIN, 12));

        initTable();

        table.getColumn(SITE_COLUMN_NAME).setCellEditor(new TableEditor(new JTextField(SITE_COLUMN_NAME), this));
        table.getColumn(LOGIN_COLUMN_NAME).setCellEditor(new TableEditor(new JTextField(LOGIN_COLUMN_NAME), this));
        table.getColumn(PASSWORD_COLUMN_NAME).setCellEditor(new TableEditor(new JTextField(PASSWORD_COLUMN_NAME), this));

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
        System.out.println("Save. To save: " + rows);

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
        System.out.println("Save. Successfully saved: " + rows);
        history.save();
        setStatus("Сохранено", STATUS_SUCCESS);
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

    @Override
    public void dispose() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();

        int called = stackTraceElements.length;
        if (called == 23
                || called == 29
                || called == 30
                || called == 43
                || called == 83
                || called == 86
                || called == 96
                || called == 100) {
            if (called == 23) {
                disposeCounter++;
                if (disposeCounter >= 2) {
                    disposeCounter = 0;
                    return;
                }
            } else {
                disposeCounter = 0;
            }

            if (!isEdited) {
                FrameUtils.unRegisterWindow(this);
                disposeFrame();
            } else {
                saveOnExitDialog = new SaveOnExitDialog(this);
                saveOnExitDialog.setVisible(true);
            }
        } else {
            System.out.println("close called>>" + called);
        }

    }

    public void disposeFrame() {
        setEdited(false);
        FrameUtils.setFrameLocation(MainForm.class.getName(), getLocation());
        FrameUtils.setFrameSize(MainForm.class.getName(), getSize());
        super.dispose();
        FrameUtils.unRegisterWindow(this);
        Core.setIsExitCanceled(false);
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

        tableModel.addColumn(SITE_COLUMN_NAME, siteData);
        tableModel.addColumn(LOGIN_COLUMN_NAME, loginData);
        tableModel.addColumn(PASSWORD_COLUMN_NAME, pwdData);
        return tableModel;
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

    private void createUIComponents() {
        if (IS_MAC) {
            addUpButton = new ControlButton();
            addDownButton = new ControlButton();
            moveUpButton = new ControlButton();
            moveDownButton = new ControlButton();
        } else {
            addUpButton = new JButton();
            addDownButton = new JButton();
            moveUpButton = new JButton();
            moveDownButton = new JButton();
        }
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
        ArrayList<Record> records = mainForm.recordArrayList;
        final String text = textField.getText();
        final int start = text.length();

        if (col == MainForm.SITE_COLUMN_INDEX ||
                col == MainForm.LOGIN_COLUMN_INDEX) {
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

            if (col == MainForm.SITE_COLUMN_INDEX) {
                for (String site : sites) {
                    if (site.startsWith(text) && site.length() > text.length()) {
                        result.add(site);
                    }
                }
            }
            if (col == MainForm.LOGIN_COLUMN_INDEX) {
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
                            System.out.println("autoComplete");
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
        return mainForm.editModeJRadioButtonMenuItem.isSelected();
    }

    private boolean isNumberCell() {
        return textField != null && mainForm.editModeJRadioButtonMenuItem.isSelected();
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

class MainFormTableHeader extends JPanel implements TableCellRenderer {
    JLabel label = new JLabel();
    JLabel sortedIcon = new JLabel();

    private JPopupMenu getPopUpMenu(final JTable table) {
        JPopupMenu popup = new JPopupMenu();
        JMenuItem cancel = new JMenuItem("Отменить сортировку");
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultRowSorter sorter = ((DefaultRowSorter) table.getRowSorter());
                ArrayList<RowSorter.SortKey> list = new ArrayList<>();
                list.add(new RowSorter.SortKey(0, SortOrder.UNSORTED));
                sorter.setSortKeys(list);
                sorter.sort();

            }
        });
        popup.add(cancel);
        return popup;
    }

    @Override
    public Component getTableCellRendererComponent(final JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {

        setLayout(new BorderLayout());

        List<? extends RowSorter.SortKey> keys = table.getRowSorter().getSortKeys();

        remove(label);

        label = new JLabel(value.toString());

        if (keys.size() > 0) {
            RowSorter.SortKey sorter = keys.get(0);
            String name = sorter.getSortOrder().name();
            int ordinal = sorter.getSortOrder().ordinal();

            if ((ordinal == 0 || ordinal == 1) && column == sorter.getColumn()) {
                label.setFont(new Font("Helvetica", Font.BOLD, 11));

                table.getTableHeader().setComponentPopupMenu(getPopUpMenu(table));
                remove(sortedIcon);
                try {
                    if (ordinal == 0) {
                        sortedIcon = new JLabel(new ImageIcon(ImageIO.read(getClass().getResource("/icons/controls/table.header.sort.asc.png"))));
                    } else {
                        sortedIcon = new JLabel(new ImageIcon(ImageIO.read(getClass().getResource("/icons/controls/table.header.sort.desc.png"))));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                add(sortedIcon, BorderLayout.EAST);
            } else {
                label.setFont(new Font("Helvetica", Font.PLAIN, 11));
                remove(sortedIcon);
            }
            MainForm.refreshLockTimer();
        } else {
            label.setFont(new Font("Helvetica", Font.PLAIN, 11));
        }

        add(label, BorderLayout.WEST);

        setBackground(new Color(246, 246, 246));

        setBorder(BorderFactory.createCompoundBorder(new TableHeaderBorder(getSize()), new EmptyBorder(0, 10, 0, 10)));
//        setToolTipText((String) value);
        return this;
    }

}

class TableHeaderBorder implements Border {
    final Color topColor = new Color(172, 172, 172);
    final Color bottomColor = new Color(196, 196, 196);
    Dimension dimension;
    Color separatorColor = new Color(220, 220, 220);


    public TableHeaderBorder(Dimension dimension) {
        this.dimension = dimension;
    }

    public TableHeaderBorder(Dimension dimension, Color separatorColor) {
        this.dimension = dimension;
        this.separatorColor = separatorColor;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        g.setColor(topColor);
        g.drawLine(x, y - 1, width, y - 1);

        g.setColor(bottomColor);
        g.drawLine(x, height - 1, width, height - 1);

        g.setColor(separatorColor);
        g.drawLine(width - 1, y + 3, width - 1, height - 4);
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(0, 0, 0, 0);
    }

    @Override
    public boolean isBorderOpaque() {
        return false;
    }
}

