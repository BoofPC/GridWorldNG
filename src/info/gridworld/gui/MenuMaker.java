/*
 * AP(r) Computer Science GridWorld Case Study: Copyright(c) 2005-2006 Cay S. Horstmann
 * (http://horstmann.com) This code is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation. This
 * code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * @author Cay Horstmann
 */
package info.gridworld.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.beans.PropertyEditorSupport;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import info.gridworld.grid.Grid;
import info.gridworld.grid.Location;

/**
 * Makes the menus for constructing new occupants and grids, and for invoking methods on existing
 * occupants. <br />
 * This code is not tested on the AP CS A and AB exams. It contains GUI implementation details that
 * are not intended to be understood by AP CS students.
 */
public class MenuMaker<T> {
  /**
   * Constructs a menu maker for a given world.
   * 
   * @param parent the frame in which the world is displayed
   * @param resources the resource bundle
   * @param displayMap the display map
   */
  public MenuMaker(final WorldFrame<T> parent, final ResourceBundle resources,
    final DisplayMap displayMap) {
    this.parent = parent;
    this.resources = resources;
    this.displayMap = displayMap;
  }

  /**
   * Makes a menu that displays all public methods of an object
   * 
   * @param occupant the object whose methods should be displayed
   * @param loc the location of the occupant
   * @return the menu to pop up
   */
  public JPopupMenu makeMethodMenu(final T occupant, final Location loc) {
    this.occupant = occupant;
    this.currentLocation = loc;
    final JPopupMenu menu = new JPopupMenu();
    final Method[] methods = this.getMethods();
    Class oldDcl = null;
    for (int i = 0; i < methods.length; i++) {
      final Class dcl = methods[i].getDeclaringClass();
      if (dcl != Object.class) {
        if (i > 0 && dcl != oldDcl) {
          menu.addSeparator();
        }
        menu.add(new MethodItem(methods[i]));
      }
      oldDcl = dcl;
    }
    return menu;
  }

  /**
   * Makes a menu that displays all public constructors of a collection of classes.
   * 
   * @param classes the classes whose constructors should be displayed
   * @param loc the location of the occupant to be constructed
   * @return the menu to pop up
   */
  public JPopupMenu makeConstructorMenu(final Collection<Class> classes,
    final Location loc) {
    this.currentLocation = loc;
    final JPopupMenu menu = new JPopupMenu();
    boolean first = true;
    final Iterator<Class> iter = classes.iterator();
    while (iter.hasNext()) {
      if (first) {
        first = false;
      } else {
        menu.addSeparator();
      }
      final Class cl = iter.next();
      final Constructor[] cons = cl.getConstructors();
      for (int i = 0; i < cons.length; i++) {
        menu.add(new OccupantConstructorItem(cons[i]));
      }
    }
    return menu;
  }

  /**
   * Adds menu items that call all public constructors of a collection of classes to a menu
   * 
   * @param menu the menu to which the items should be added
   * @param classes the collection of classes
   */
  public void addConstructors(final JMenu menu,
    final Collection<Class> classes) {
    boolean first = true;
    final Iterator<Class> iter = classes.iterator();
    while (iter.hasNext()) {
      if (first) {
        first = false;
      } else {
        menu.addSeparator();
      }
      final Class cl = iter.next();
      final Constructor[] cons = cl.getConstructors();
      for (int i = 0; i < cons.length; i++) {
        menu.add(new GridConstructorItem(cons[i]));
      }
    }
  }

  private Method[] getMethods() {
    final Class cl = this.occupant.getClass();
    final Method[] methods = cl.getMethods();
    Arrays.sort(methods, new Comparator<Method>() {
      @Override
      public int compare(final Method m1, final Method m2) {
        int d1 = this.depth(m1.getDeclaringClass());
        int d2 = this.depth(m2.getDeclaringClass());
        if (d1 != d2) {
          return d2 - d1;
        }
        final int d = m1.getName().compareTo(m2.getName());
        if (d != 0) {
          return d;
        }
        d1 = m1.getParameterTypes().length;
        d2 = m2.getParameterTypes().length;
        return d1 - d2;
      }

      private int depth(final Class cl) {
        if (cl == null) {
          return 0;
        } else {
          return 1 + this.depth(cl.getSuperclass());
        }
      }
    });
    return methods;
  }

  /**
   * A menu item that shows a method or constructor.
   */
  private class MCItem extends JMenuItem {
    public String getDisplayString(final Class retType, final String name,
      final Class[] paramTypes) {
      final StringBuffer b = new StringBuffer();
      b.append("<html>");
      if (retType != null) {
        this.appendTypeName(b, retType.getName());
      }
      b.append(" <font color='blue'>");
      this.appendTypeName(b, name);
      b.append("</font>( ");
      for (int i = 0; i < paramTypes.length; i++) {
        if (i > 0) {
          b.append(", ");
        }
        this.appendTypeName(b, paramTypes[i].getName());
      }
      b.append(" )</html>");
      return b.toString();
    }

    public void appendTypeName(final StringBuffer b, final String name) {
      final int i = name.lastIndexOf('.');
      if (i >= 0) {
        final String prefix = name.substring(0, i + 1);
        if (!prefix.equals("java.lang")) {
          b.append("<font color='gray'>");
          b.append(prefix);
          b.append("</font>");
        }
        b.append(name.substring(i + 1));
      } else {
        b.append(name);
      }
    }

    public Object makeDefaultValue(final Class type) {
      if (type == int.class) {
        return new Integer(0);
      } else if (type == boolean.class) {
        return Boolean.FALSE;
      } else if (type == double.class) {
        return new Double(0);
      } else if (type == String.class) {
        return "";
      } else if (type == Color.class) {
        return Color.BLACK;
      } else if (type == Location.class) {
        return MenuMaker.this.currentLocation;
      } else if (Grid.class.isAssignableFrom(type)) {
        return MenuMaker.this.currentGrid;
      } else {
        try {
          return type.newInstance();
        } catch (final Exception ex) {
          return null;
        }
      }
    }
  }
  private abstract class ConstructorItem extends MCItem {
    public ConstructorItem(final Constructor c) {
      this.setText(this.getDisplayString(null, c.getDeclaringClass().getName(),
        c.getParameterTypes()));
      this.c = c;
    }

    public Object invokeConstructor() {
      final Class[] types = this.c.getParameterTypes();
      Object[] values = new Object[types.length];
      for (int i = 0; i < types.length; i++) {
        values[i] = this.makeDefaultValue(types[i]);
      }
      if (types.length > 0) {
        final PropertySheet sheet = new PropertySheet(types, values);
        JOptionPane.showMessageDialog(this, sheet,
          MenuMaker.this.resources.getString("dialog.method.params"),
          JOptionPane.QUESTION_MESSAGE);
        values = sheet.getValues();
      }
      try {
        return this.c.newInstance(values);
      } catch (final InvocationTargetException ex) {
        MenuMaker.this.parent.new GUIExceptionHandler().handle(ex.getCause());
        return null;
      } catch (final Exception ex) {
        MenuMaker.this.parent.new GUIExceptionHandler().handle(ex);
        return null;
      }
    }

    private final Constructor c;
  }
  private class OccupantConstructorItem extends ConstructorItem
    implements ActionListener {
    public OccupantConstructorItem(final Constructor c) {
      super(c);
      this.addActionListener(this);
      this.setIcon(
        MenuMaker.this.displayMap.getIcon(c.getDeclaringClass(), 16, 16));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void actionPerformed(final ActionEvent event) {
      final T result = (T) this.invokeConstructor();
      MenuMaker.this.parent.getWorld().add(MenuMaker.this.currentLocation,
        result);
      MenuMaker.this.parent.repaint();
    }
  }
  private class GridConstructorItem extends ConstructorItem
    implements ActionListener {
    public GridConstructorItem(final Constructor c) {
      super(c);
      this.addActionListener(this);
      this.setIcon(
        MenuMaker.this.displayMap.getIcon(c.getDeclaringClass(), 16, 16));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void actionPerformed(final ActionEvent event) {
      final Grid<T> newGrid = (Grid<T>) this.invokeConstructor();
      MenuMaker.this.parent.setGrid(newGrid);
    }
  }
  private class MethodItem extends MCItem implements ActionListener {
    public MethodItem(final Method m) {
      this.setText(this.getDisplayString(m.getReturnType(), m.getName(),
        m.getParameterTypes()));
      this.m = m;
      this.addActionListener(this);
      this.setIcon(
        MenuMaker.this.displayMap.getIcon(m.getDeclaringClass(), 16, 16));
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
      final Class[] types = this.m.getParameterTypes();
      Object[] values = new Object[types.length];
      for (int i = 0; i < types.length; i++) {
        values[i] = this.makeDefaultValue(types[i]);
      }
      if (types.length > 0) {
        final PropertySheet sheet = new PropertySheet(types, values);
        JOptionPane.showMessageDialog(this, sheet,
          MenuMaker.this.resources.getString("dialog.method.params"),
          JOptionPane.QUESTION_MESSAGE);
        values = sheet.getValues();
      }
      try {
        final Object result = this.m.invoke(MenuMaker.this.occupant, values);
        MenuMaker.this.parent.repaint();
        if (this.m.getReturnType() != void.class) {
          final String resultString = result.toString();
          Object resultObject;
          final int MAX_LENGTH = 50;
          final int MAX_HEIGHT = 10;
          if (resultString.length() < MAX_LENGTH) {
            resultObject = resultString;
          } else {
            final int rows =
              Math.min(MAX_HEIGHT, 1 + resultString.length() / MAX_LENGTH);
            final JTextArea pane = new JTextArea(rows, MAX_LENGTH);
            pane.setText(resultString);
            pane.setLineWrap(true);
            resultObject = new JScrollPane(pane);
          }
          JOptionPane.showMessageDialog(MenuMaker.this.parent, resultObject,
            MenuMaker.this.resources.getString("dialog.method.return"),
            JOptionPane.INFORMATION_MESSAGE);
        }
      } catch (final InvocationTargetException ex) {
        MenuMaker.this.parent.new GUIExceptionHandler().handle(ex.getCause());
      } catch (final Exception ex) {
        MenuMaker.this.parent.new GUIExceptionHandler().handle(ex);
      }
    }

    private final Method m;
  }

  private T occupant;
  private Grid currentGrid;
  private Location currentLocation;
  private final WorldFrame<T> parent;
  private final DisplayMap displayMap;
  private final ResourceBundle resources;
}


class PropertySheet extends JPanel {
  /**
   * Constructs a property sheet that shows the editable properties of a given object.
   * 
   * @param object the object whose properties are being edited
   */
  public PropertySheet(final Class[] types, final Object[] values) {
    this.values = values;
    this.editors = new PropertyEditor[types.length];
    this.setLayout(new FormLayout());
    for (int i = 0; i < values.length; i++) {
      final JLabel label = new JLabel(types[i].getName());
      this.add(label);
      if (Grid.class.isAssignableFrom(types[i])) {
        label.setEnabled(false);
        this.add(new JPanel());
      } else {
        this.editors[i] = this.getEditor(types[i]);
        if (this.editors[i] != null) {
          this.editors[i].setValue(values[i]);
          this.add(this.getEditorComponent(this.editors[i]));
        } else {
          this.add(new JLabel("?"));
        }
      }
    }
  }

  /**
   * Gets the property editor for a given property, and wires it so that it updates the given
   * object.
   * 
   * @param bean the object whose properties are being edited
   * @param descriptor the descriptor of the property to be edited
   * @return a property editor that edits the property with the given descriptor and updates the
   *         given object
   */
  public PropertyEditor getEditor(final Class type) {
    PropertyEditor editor;
    editor = PropertySheet.defaultEditors.get(type);
    if (editor != null) {
      return editor;
    }
    editor = PropertyEditorManager.findEditor(type);
    return editor;
  }

  /**
   * Wraps a property editor into a component.
   * 
   * @param editor the editor to wrap
   * @return a button (if there is a custom editor), combo box (if the editor has tags), or text
   *         field (otherwise)
   */
  public Component getEditorComponent(final PropertyEditor editor) {
    final String[] tags = editor.getTags();
    final String text = editor.getAsText();
    if (editor.supportsCustomEditor()) {
      return editor.getCustomEditor();
    } else if (tags != null) {
      // make a combo box that shows all tags
      final JComboBox comboBox = new JComboBox(tags);
      comboBox.setSelectedItem(text);
      comboBox.addItemListener(event -> {
        if (event.getStateChange() == ItemEvent.SELECTED) {
          editor.setAsText((String) comboBox.getSelectedItem());
        }
      });
      return comboBox;
    } else {
      final JTextField textField = new JTextField(text, 10);
      textField.getDocument().addDocumentListener(new DocumentListener() {
        @Override
        public void insertUpdate(final DocumentEvent e) {
          try {
            editor.setAsText(textField.getText());
          } catch (final IllegalArgumentException exception) {
          }
        }

        @Override
        public void removeUpdate(final DocumentEvent e) {
          try {
            editor.setAsText(textField.getText());
          } catch (final IllegalArgumentException exception) {
          }
        }

        @Override
        public void changedUpdate(final DocumentEvent e) {}
      });
      return textField;
    }
  }

  public Object[] getValues() {
    for (int i = 0; i < this.editors.length; i++) {
      if (this.editors[i] != null) {
        this.values[i] = this.editors[i].getValue();
      }
    }
    return this.values;
  }

  private final PropertyEditor[] editors;
  private final Object[] values;
  private static Map<Class, PropertyEditor> defaultEditors;

  // workaround for Web Start bug
  public static class StringEditor extends PropertyEditorSupport {
    @Override
    public String getAsText() {
      return (String) this.getValue();
    }

    @Override
    public void setAsText(final String s) {
      this.setValue(s);
    }
  }

  static {
    PropertySheet.defaultEditors = new HashMap<Class, PropertyEditor>();
    PropertySheet.defaultEditors.put(String.class, new StringEditor());
    PropertySheet.defaultEditors.put(Location.class, new LocationEditor());
    PropertySheet.defaultEditors.put(Color.class, new ColorEditor());
  }
}
