/*
 * AP(r) Computer Science GridWorld Case Study: Copyright(c) 2002-2006 College Entrance Examination
 * Board (http://www.collegeboard.com). This code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation. This code is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU General Public License for more details.
 * @author Julie Zelenski
 * @author Cay Horstmann
 */
package info.gridworld.gui;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Modifier;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.KeyStroke;
import javax.swing.Timer;

import info.gridworld.grid.Grid;
import info.gridworld.grid.Location;
import info.gridworld.world.World;

/**
 * The GUIController controls the behavior in a WorldFrame. <br />
 * This code is not tested on the AP CS A and AB exams. It contains GUI implementation details that
 * are not intended to be understood by AP CS students.
 */
public class GUIController<T> {
  public static final int INDEFINITE = 0, FIXED_STEPS = 1, PROMPT_STEPS = 2;
  private static final int MIN_DELAY_MSECS = 10, MAX_DELAY_MSECS = 1000;
  private static final int INITIAL_DELAY = GUIController.MIN_DELAY_MSECS
    + (GUIController.MAX_DELAY_MSECS - GUIController.MIN_DELAY_MSECS) / 2;
  private Timer timer;
  private JButton stepButton, runButton, stopButton;
  private JComponent controlPanel;
  private GridPanel display;
  private WorldFrame<T> parentFrame;
  private int numStepsToRun, numStepsSoFar;
  private ResourceBundle resources;
  private DisplayMap displayMap;
  private boolean running;
  private Set<Class> occupantClasses;

  /**
   * Creates a new controller tied to the specified display and gui frame.
   * 
   * @param parent the frame for the world window
   * @param disp the panel that displays the grid
   * @param displayMap the map for occupant displays
   * @param res the resource bundle for message display
   */
  public GUIController(final WorldFrame<T> parent, final GridPanel disp,
    final DisplayMap displayMap, final ResourceBundle res) {
    this.resources = res;
    this.display = disp;
    this.parentFrame = parent;
    this.displayMap = displayMap;
    this.makeControls();
    this.occupantClasses =
      new TreeSet<Class>((a, b) -> a.getName().compareTo(b.getName()));
    final World<T> world = this.parentFrame.getWorld();
    final Grid<T> gr = world.getGrid();
    for (final Location loc : gr.getOccupiedLocations()) {
      this.addOccupant(gr.get(loc));
    }
    for (final String name : world.getOccupantClasses()) {
      try {
        this.occupantClasses.add(Class.forName(name));
      } catch (final Exception ex) {
        ex.printStackTrace();
      }
    }
    this.timer =
      new Timer(GUIController.INITIAL_DELAY, evt -> GUIController.this.step());
    this.display.addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(final MouseEvent evt) {
        final Grid<T> gr = GUIController.this.parentFrame.getWorld().getGrid();
        final Location loc =
          GUIController.this.display.locationForPoint(evt.getPoint());
        if (loc != null && gr.isValid(loc) && !GUIController.this.isRunning()) {
          GUIController.this.display.setCurrentLocation(loc);
          GUIController.this.locationClicked();
        }
      }
    });
    this.stop();
  }

  /**
   * Advances the world one step.
   */
  public void step() {
    this.parentFrame.getWorld().step();
    this.parentFrame.repaint();
    if (++this.numStepsSoFar == this.numStepsToRun) {
      this.stop();
    }
    final Grid<T> gr = this.parentFrame.getWorld().getGrid();
    for (final Location loc : gr.getOccupiedLocations()) {
      this.addOccupant(gr.get(loc));
    }
  }

  private void addOccupant(final T occupant) {
    Class cl = occupant.getClass();
    do {
      if ((cl.getModifiers() & Modifier.ABSTRACT) == 0) {
        this.occupantClasses.add(cl);
      }
      cl = cl.getSuperclass();
    } while (cl != Object.class);
  }

  /**
   * Starts a timer to repeatedly carry out steps at the speed currently indicated by the speed
   * slider up Depending on the run option, it will either carry out steps for some fixed number or
   * indefinitely until stopped.
   */
  public void run() {
    this.display.setToolTipsEnabled(false); // hide tool tips while running
    this.parentFrame.setRunMenuItemsEnabled(false);
    this.stopButton.setEnabled(true);
    this.stepButton.setEnabled(false);
    this.runButton.setEnabled(false);
    this.numStepsSoFar = 0;
    this.timer.start();
    this.running = true;
  }

  /**
   * Stops any existing timer currently carrying out steps.
   */
  public void stop() {
    this.display.setToolTipsEnabled(true);
    this.parentFrame.setRunMenuItemsEnabled(true);
    this.timer.stop();
    this.stopButton.setEnabled(false);
    this.runButton.setEnabled(true);
    this.stepButton.setEnabled(true);
    this.running = false;
  }

  public boolean isRunning() {
    return this.running;
  }

  /**
   * Builds the panel with the various controls (buttons and slider).
   */
  private void makeControls() {
    this.controlPanel = new JPanel();
    this.stepButton = new JButton(this.resources.getString("button.gui.step"));
    this.runButton = new JButton(this.resources.getString("button.gui.run"));
    this.stopButton = new JButton(this.resources.getString("button.gui.stop"));
    this.controlPanel
      .setLayout(new BoxLayout(this.controlPanel, BoxLayout.X_AXIS));
    this.controlPanel.setBorder(BorderFactory.createEtchedBorder());
    final Dimension spacer =
      new Dimension(5, this.stepButton.getPreferredSize().height + 10);
    this.controlPanel.add(Box.createRigidArea(spacer));
    this.controlPanel.add(this.stepButton);
    this.controlPanel.add(Box.createRigidArea(spacer));
    this.controlPanel.add(this.runButton);
    this.controlPanel.add(Box.createRigidArea(spacer));
    this.controlPanel.add(this.stopButton);
    this.runButton.setEnabled(false);
    this.stepButton.setEnabled(false);
    this.stopButton.setEnabled(false);
    this.controlPanel.add(Box.createRigidArea(spacer));
    this.controlPanel
      .add(new JLabel(this.resources.getString("slider.gui.slow")));
    final JSlider speedSlider = new JSlider(GUIController.MIN_DELAY_MSECS,
      GUIController.MAX_DELAY_MSECS, GUIController.INITIAL_DELAY);
    speedSlider.setInverted(true);
    speedSlider.setPreferredSize(
      new Dimension(100, speedSlider.getPreferredSize().height));
    speedSlider.setMaximumSize(speedSlider.getPreferredSize());
    // remove control PAGE_UP, PAGE_DOWN from slider--they should be used
    // for zoom
    InputMap map = speedSlider.getInputMap();
    while (map != null) {
      map.remove(KeyStroke.getKeyStroke("control PAGE_UP"));
      map.remove(KeyStroke.getKeyStroke("control PAGE_DOWN"));
      map = map.getParent();
    }
    this.controlPanel.add(speedSlider);
    this.controlPanel
      .add(new JLabel(this.resources.getString("slider.gui.fast")));
    this.controlPanel.add(Box.createRigidArea(new Dimension(5, 0)));
    this.stepButton.addActionListener(e -> GUIController.this.step());
    this.runButton.addActionListener(e -> GUIController.this.run());
    this.stopButton.addActionListener(e -> GUIController.this.stop());
    speedSlider.addChangeListener(evt -> GUIController.this.timer
      .setDelay(((JSlider) evt.getSource()).getValue()));
  }

  /**
   * Returns the panel containing the controls.
   * 
   * @return the control panel
   */
  public JComponent controlPanel() {
    return this.controlPanel;
  }

  /**
   * Callback on mousePressed when editing a grid.
   */
  private void locationClicked() {
    final World<T> world = this.parentFrame.getWorld();
    final Location loc = this.display.getCurrentLocation();
    if (loc != null && !world.locationClicked(loc)) {
      this.editLocation();
    }
    this.parentFrame.repaint();
  }

  /**
   * Edits the contents of the current location, by displaying the constructor or method menu.
   */
  public void editLocation() {
    final World<T> world = this.parentFrame.getWorld();
    final Location loc = this.display.getCurrentLocation();
    if (loc != null) {
      final T occupant = world.getGrid().get(loc);
      if (occupant == null) {
        final MenuMaker<T> maker =
          new MenuMaker<T>(this.parentFrame, this.resources, this.displayMap);
        final JPopupMenu popup =
          maker.makeConstructorMenu(this.occupantClasses, loc);
        final Point p = this.display.pointForLocation(loc);
        popup.show(this.display, p.x, p.y);
      } else {
        final MenuMaker<T> maker =
          new MenuMaker<T>(this.parentFrame, this.resources, this.displayMap);
        final JPopupMenu popup = maker.makeMethodMenu(occupant, loc);
        final Point p = this.display.pointForLocation(loc);
        popup.show(this.display, p.x, p.y);
      }
    }
    this.parentFrame.repaint();
  }

  /**
   * Edits the contents of the current location, by displaying the constructor or method menu.
   */
  public void deleteLocation() {
    final World<T> world = this.parentFrame.getWorld();
    final Location loc = this.display.getCurrentLocation();
    if (loc != null) {
      world.remove(loc);
      this.parentFrame.repaint();
    }
  }
}
