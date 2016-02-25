/*
 * AP(r) Computer Science GridWorld Case Study: Copyright(c) 2002-2006 College Entrance Examination
 * Board (http://www.collegeboard.com). This code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation. This code is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU General Public License for more details.
 * @author Julie Zelenski
 * @author Chris Nevison
 * @author Cay Horstmann
 */
package info.gridworld.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.event.HyperlinkEvent;

import info.gridworld.grid.Grid;
import info.gridworld.grid.Location;
import info.gridworld.world.World;

/**
 * The WorldFrame displays a World and allows manipulation of its occupants. <br />
 * This code is not tested on the AP CS A and AB exams. It contains GUI implementation details that
 * are not intended to be understood by AP CS students.
 */
public class WorldFrame<T> extends JFrame {
  private static final long serialVersionUID = 1L;
  private GUIController<T> control;
  private GridPanel display;
  private JTextArea messageArea;
  private ArrayList<JMenuItem> menuItemsDisabledDuringRun;
  private World<T> world;
  private ResourceBundle resources;
  private DisplayMap displayMap;
  private Set<Class<?>> gridClasses;
  private JMenu newGridMenu;
  private static int count = 0;

  /**
   * Constructs a WorldFrame that displays the occupants of a world
   * 
   * @param world the world to display
   */
  public WorldFrame(final World<T> world) {
    this.world = world;
    WorldFrame.count++;
    this.resources =
      ResourceBundle.getBundle(this.getClass().getName() + "Resources");
    try {
      System.setProperty("sun.awt.exception.handler",
        GUIExceptionHandler.class.getName());
    } catch (final SecurityException ex) {
      // will fail in an applet
    }
    this.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(final WindowEvent event) {
        WorldFrame.count--;
        if (WorldFrame.count == 0) {
          System.exit(0);
        }
      }
    });
    this.displayMap = new DisplayMap();
    String title = System.getProperty("info.gridworld.gui.frametitle");
    if (title == null) {
      title = this.resources.getString("frame.title");
    }
    this.setTitle(title);
    this.setLocation(25, 15);
    final URL appIconUrl = this.getClass().getResource("GridWorld.gif");
    final ImageIcon appIcon = new ImageIcon(appIconUrl);
    this.setIconImage(appIcon.getImage());
    this.makeMenus();
    final JPanel content = new JPanel();
    content.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
    content.setLayout(new BorderLayout());
    this.setContentPane(content);
    this.display = new GridPanel(this.displayMap, this.resources);
    KeyboardFocusManager.getCurrentKeyboardFocusManager()
      .addKeyEventDispatcher(event -> {
        if (WorldFrame.this.getFocusOwner() == null) {
          return false;
        }
        String text = KeyStroke.getKeyStrokeForEvent(event).toString();
        final String PRESSED = "pressed ";
        final int n = text.indexOf(PRESSED);
        if (n < 0) {
          return false;
        }
        // filter out modifier keys; they are neither characters or actions
        if (event.getKeyChar() == KeyEvent.CHAR_UNDEFINED
          && !event.isActionKey()) {
          return false;
        }
        text = text.substring(0, n) + text.substring(n + PRESSED.length());
        final boolean consumed = WorldFrame.this.getWorld().keyPressed(text,
          WorldFrame.this.display.getCurrentLocation());
        if (consumed) {
          WorldFrame.this.repaint();
        }
        return consumed;
      });
    final JScrollPane scrollPane = new JScrollPane();
    scrollPane.setViewport(new PseudoInfiniteViewport(scrollPane));
    scrollPane.setViewportView(this.display);
    content.add(scrollPane, BorderLayout.CENTER);
    this.gridClasses =
      new TreeSet<Class<?>>((a, b) -> a.getName().compareTo(b.getName()));
    for (final String name : world.getGridClasses()) {
      try {
        this.gridClasses.add(Class.forName(name));
      } catch (final Exception ex) {
        ex.printStackTrace();
      }
    }
    final Grid<T> gr = world.getGrid();
    this.gridClasses.add(gr.getClass());
    this.makeNewGridMenu();
    this.control =
      new GUIController<T>(this, this.display, this.displayMap, this.resources);
    content.add(this.control.controlPanel(), BorderLayout.SOUTH);
    this.messageArea = new JTextArea(2, 35);
    this.messageArea.setEditable(false);
    this.messageArea.setFocusable(false);
    this.messageArea.setBackground(new Color(0xFAFAD2));
    content.add(new JScrollPane(this.messageArea), BorderLayout.NORTH);
    this.pack();
    this.repaint(); // to show message
    this.display.setGrid(gr);
  }

  @Override
  public void repaint() {
    String message = this.getWorld().getMessage();
    if (message == null) {
      message = this.resources.getString("message.default");
    }
    this.messageArea.setText(message);
    this.messageArea.repaint();
    this.display.repaint(); // for applet
    super.repaint();
  }

  /**
   * Gets the world that this frame displays
   * 
   * @return the world
   */
  public World<T> getWorld() {
    return this.world;
  }

  /**
   * Sets a new grid for this world. Occupants are transferred from the old world to the new.
   * 
   * @param newGrid the new grid
   */
  public void setGrid(final Grid<T> newGrid) {
    final Grid<T> oldGrid = this.world.getGrid();
    final Map<Location, T> occupants = new HashMap<Location, T>();
    for (final Location loc : oldGrid.getOccupiedLocations()) {
      occupants.put(loc, this.world.remove(loc));
    }
    this.world.setGrid(newGrid);
    for (final Location loc : occupants.keySet()) {
      if (newGrid.isValid(loc)) {
        this.world.add(loc, occupants.get(loc));
      }
    }
    this.display.setGrid(newGrid);
    this.repaint();
  }

  /**
   * Displays an error message
   * 
   * @param t the throwable that describes the error
   * @param resource the resource whose .text/.title strings should be used in the dialog
   */
  public void showError(final Throwable t, final String resource) {
    String text;
    try {
      text = this.resources.getString(resource + ".text");
    } catch (final MissingResourceException e) {
      text = this.resources.getString("error.text");
    }
    String title;
    try {
      title = this.resources.getString(resource + ".title");
    } catch (final MissingResourceException e) {
      title = this.resources.getString("error.title");
    }
    final String reason = this.resources.getString("error.reason");
    final String message =
      text + "\n" + MessageFormat.format(reason, new Object[] {t});
    JOptionPane.showMessageDialog(this, message, title,
      JOptionPane.ERROR_MESSAGE);
  }

  // Creates the drop-down menus on the frame.
  private JMenu makeMenu(final String resource) {
    final JMenu menu = new JMenu();
    this.configureAbstractButton(menu, resource);
    return menu;
  }

  private JMenuItem makeMenuItem(final String resource,
    final ActionListener listener) {
    final JMenuItem item = new JMenuItem();
    this.configureMenuItem(item, resource, listener);
    return item;
  }

  private void configureMenuItem(final JMenuItem item, final String resource,
    final ActionListener listener) {
    this.configureAbstractButton(item, resource);
    item.addActionListener(listener);
    try {
      final String accel = this.resources.getString(resource + ".accel");
      final String metaPrefix = "@";
      if (accel.startsWith(metaPrefix)) {
        final int menuMask = this.getToolkit().getMenuShortcutKeyMask();
        final KeyStroke key = KeyStroke.getKeyStroke(KeyStroke
          .getKeyStroke(accel.substring(metaPrefix.length())).getKeyCode(),
          menuMask);
        item.setAccelerator(key);
      } else {
        item.setAccelerator(KeyStroke.getKeyStroke(accel));
      }
    } catch (final MissingResourceException ex) {
      // no accelerator
    }
  }

  private void configureAbstractButton(final AbstractButton button,
    final String resource) {
    String title = this.resources.getString(resource);
    final int i = title.indexOf('&');
    int mnemonic = 0;
    if (i >= 0) {
      mnemonic = title.charAt(i + 1);
      title = title.substring(0, i) + title.substring(i + 1);
      button.setText(title);
      button.setMnemonic(Character.toUpperCase(mnemonic));
      button.setDisplayedMnemonicIndex(i);
    } else {
      button.setText(title);
    }
  }

  private void makeMenus() {
    final JMenuBar mbar = new JMenuBar();
    JMenu menu;
    this.menuItemsDisabledDuringRun = new ArrayList<JMenuItem>();
    mbar.add(menu = this.makeMenu("menu.file"));
    this.newGridMenu = this.makeMenu("menu.file.new");
    menu.add(this.newGridMenu);
    this.menuItemsDisabledDuringRun.add(this.newGridMenu);
    menu.add(this.makeMenuItem("menu.file.quit", e -> System.exit(0)));
    mbar.add(menu = this.makeMenu("menu.view"));
    menu.add(this.makeMenuItem("menu.view.up",
      e -> WorldFrame.this.display.moveLocation(-1, 0)));
    menu.add(this.makeMenuItem("menu.view.down",
      e -> WorldFrame.this.display.moveLocation(1, 0)));
    menu.add(this.makeMenuItem("menu.view.left",
      e -> WorldFrame.this.display.moveLocation(0, -1)));
    menu.add(this.makeMenuItem("menu.view.right",
      e -> WorldFrame.this.display.moveLocation(0, 1)));
    JMenuItem viewEditMenu;
    menu.add(viewEditMenu = this.makeMenuItem("menu.view.edit",
      e -> WorldFrame.this.control.editLocation()));
    this.menuItemsDisabledDuringRun.add(viewEditMenu);
    JMenuItem viewDeleteMenu;
    menu.add(viewDeleteMenu = this.makeMenuItem("menu.view.delete",
      e -> WorldFrame.this.control.deleteLocation()));
    this.menuItemsDisabledDuringRun.add(viewDeleteMenu);
    menu.add(this.makeMenuItem("menu.view.zoomin",
      e -> WorldFrame.this.display.zoomIn()));
    menu.add(this.makeMenuItem("menu.view.zoomout",
      e -> WorldFrame.this.display.zoomOut()));
    mbar.add(menu = this.makeMenu("menu.help"));
    menu.add(this.makeMenuItem("menu.help.about",
      e -> WorldFrame.this.showAboutPanel()));
    menu.add(
      this.makeMenuItem("menu.help.help", e -> WorldFrame.this.showHelp()));
    menu.add(this.makeMenuItem("menu.help.license",
      e -> WorldFrame.this.showLicense()));
    this.setRunMenuItemsEnabled(true);
    this.setJMenuBar(mbar);
  }

  private void makeNewGridMenu() {
    this.newGridMenu.removeAll();
    final MenuMaker<T> maker =
      new MenuMaker<T>(this, this.resources, this.displayMap);
    maker.addConstructors(this.newGridMenu, this.gridClasses);
  }

  /**
   * Sets the enabled status of those menu items that are disabled when running.
   * 
   * @param enable true to enable the menus
   */
  public void setRunMenuItemsEnabled(final boolean enable) {
    for (final JMenuItem item : this.menuItemsDisabledDuringRun) {
      item.setEnabled(enable);
    }
  }

  /**
   * Brings up a simple dialog with some general information.
   */
  private void showAboutPanel() {
    String html =
      MessageFormat.format(this.resources.getString("dialog.about.text"),
        new Object[] {this.resources.getString("version.id")});
    final String[] props =
      {"java.version", "java.vendor", "java.home", "os.name", "os.arch",
          "os.version", "user.name", "user.home", "user.dir"};
    html += "<table border='1'>";
    for (final String prop : props) {
      try {
        final String value = System.getProperty(prop);
        html += "<tr><td>" + prop + "</td><td>" + value + "</td></tr>";
      } catch (final SecurityException ex) {
        // oh well...
      }
    }
    html += "</table>";
    html = "<html>" + html + "</html>";
    JOptionPane.showMessageDialog(this, new JLabel(html),
      this.resources.getString("dialog.about.title"),
      JOptionPane.INFORMATION_MESSAGE);
  }

  /**
   * Brings up a window with a scrolling text pane that display the help information.
   */
  private void showHelp() {
    final JDialog dialog =
      new JDialog(this, this.resources.getString("dialog.help.title"));
    final JEditorPane helpText = new JEditorPane();
    try {
      final URL url = this.getClass().getResource("GridWorldHelp.html");
      helpText.setPage(url);
    } catch (final Exception e) {
      helpText.setText(this.resources.getString("dialog.help.error"));
    }
    helpText.setEditable(false);
    helpText.addHyperlinkListener(ev -> {
      if (ev.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
        try {
          helpText.setPage(ev.getURL());
        } catch (final Exception ex) {
        }
      }
    });
    final JScrollPane sp = new JScrollPane(helpText);
    sp.setPreferredSize(new Dimension(650, 500));
    dialog.getContentPane().add(sp);
    dialog.setLocation(this.getX() + this.getWidth() - 200, this.getY() + 50);
    dialog.pack();
    dialog.setVisible(true);
  }

  /**
   * Brings up a dialog that displays the license.
   */
  private void showLicense() {
    final JDialog dialog =
      new JDialog(this, this.resources.getString("dialog.license.title"));
    final JEditorPane text = new JEditorPane();
    try {
      final URL url = this.getClass().getResource("GNULicense.txt");
      text.setPage(url);
    } catch (final Exception e) {
      text.setText(this.resources.getString("dialog.license.error"));
    }
    text.setEditable(false);
    final JScrollPane sp = new JScrollPane(text);
    sp.setPreferredSize(new Dimension(650, 500));
    dialog.getContentPane().add(sp);
    dialog.setLocation(this.getX() + this.getWidth() - 200, this.getY() + 50);
    dialog.pack();
    dialog.setVisible(true);
  }

  /**
   * Nested class that is registered as the handler for exceptions on the Swing event thread. The
   * handler will put up an alert panel and dump the stack trace to the console.
   */
  public class GUIExceptionHandler {
    public void handle(final Throwable e) {
      e.printStackTrace();
      final JTextArea area = new JTextArea(10, 40);
      final StringWriter writer = new StringWriter();
      e.printStackTrace(new PrintWriter(writer));
      area.setText(writer.toString());
      area.setCaretPosition(0);
      final String copyOption =
        WorldFrame.this.resources.getString("dialog.error.copy");
      final JOptionPane pane =
        new JOptionPane(new JScrollPane(area), JOptionPane.ERROR_MESSAGE,
          JOptionPane.YES_NO_OPTION, null, new String[] {copyOption,
              WorldFrame.this.resources.getString("cancel")});
      pane.createDialog(WorldFrame.this, e.toString()).setVisible(true);
      if (copyOption.equals(pane.getValue())) {
        area.setSelectionStart(0);
        area.setSelectionEnd(area.getText().length());
        area.copy(); // copy to clipboard
      }
    }
  }
}
