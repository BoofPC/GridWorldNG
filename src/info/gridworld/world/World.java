/*
 * AP(r) Computer Science GridWorld Case Study: Copyright(c) 2005-2006 Cay S. Horstmann
 * (http://horstmann.com) This code is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation. This
 * code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * @author Cay Horstmann
 */
package info.gridworld.world;

import java.util.ArrayList;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JFrame;

import info.gridworld.grid.BoundedGrid;
import info.gridworld.grid.Grid;
import info.gridworld.grid.Location;
import info.gridworld.gui.WorldFrame;

/**
 * A <code>World</code> is the mediator between a grid and the GridWorld GUI. <br />
 * This class is not tested on the AP CS A and AB exams.
 */
public class World<T> {
  private Grid<T> gr;
  private Set<String> occupantClassNames;
  private Set<String> gridClassNames;
  private String message;
  private JFrame frame;
  private static Random generator = new Random();
  private static final int DEFAULT_ROWS = 10;
  private static final int DEFAULT_COLS = 10;

  public World() {
    this(new BoundedGrid<T>(World.DEFAULT_ROWS, World.DEFAULT_COLS));
    this.message = null;
  }

  public World(final Grid<T> g) {
    this.gr = g;
    this.gridClassNames = new TreeSet<String>();
    this.occupantClassNames = new TreeSet<String>();
    this.addGridClass("info.gridworld.grid.BoundedGrid");
    this.addGridClass("info.gridworld.grid.UnboundedGrid");
  }

  /**
   * Constructs and shows a frame for this world.
   */
  public void show() {
    if (this.frame == null) {
      this.frame = new WorldFrame<T>(this);
      this.frame.setVisible(true);
    } else {
      this.frame.repaint();
    }
  }

  /**
   * Gets the grid managed by this world.
   * 
   * @return the grid
   */
  public Grid<T> getGrid() {
    return this.gr;
  }

  /**
   * Sets the grid managed by this world.
   * 
   * @param newGrid the new grid
   */
  public void setGrid(final Grid<T> newGrid) {
    this.gr = newGrid;
    this.repaint();
  }

  /**
   * Sets the message to be displayed in the world frame above the grid.
   * 
   * @param newMessage the new message
   */
  public void setMessage(final String newMessage) {
    this.message = newMessage;
    this.repaint();
  }

  /**
   * Gets the message to be displayed in the world frame above the grid.
   * 
   * @return the message
   */
  public String getMessage() {
    return this.message;
  }

  /**
   * This method is called when the user clicks on the step button, or when run mode has been
   * activated by clicking the run button.
   */
  public void step() {
    this.repaint();
  }

  /**
   * This method is called when the user clicks on a location in the WorldFrame.
   * 
   * @param loc the grid location that the user selected
   * @return true if the world consumes the click, or false if the GUI should invoke the
   *         Location->Edit menu action
   */
  public boolean locationClicked(final Location loc) {
    return false;
  }

  /**
   * This method is called when a key was pressed. Override it if your world wants to consume some
   * keys (e.g. "1"-"9" for Sudoku). Don't consume plain arrow keys, or the user loses the ability
   * to move the selection square with the keyboard.
   * 
   * @param description the string describing the key, in <a href=
   *          "http://java.sun.com/javase/6/docs/api/javax/swing/KeyStroke.html#getKeyStroke(java.lang.String)">
   *          this format</a>.
   * @param loc the selected location in the grid at the time the key was pressed
   * @return true if the world consumes the key press, false if the GUI should consume it.
   */
  public boolean keyPressed(final String description, final Location loc) {
    return false;
  }

  /**
   * Gets a random empty location in this world.
   * 
   * @return a random empty location
   */
  public Location getRandomEmptyLocation() {
    final Grid<T> gr = this.getGrid();
    final int rows = gr.getNumRows();
    final int cols = gr.getNumCols();
    if (rows > 0 && cols > 0) // bounded grid
    {
      // get all valid empty locations and pick one at random
      final ArrayList<Location> emptyLocs = new ArrayList<Location>();
      for (int i = 0; i < rows; i++) {
        for (int j = 0; j < cols; j++) {
          final Location loc = new Location(i, j);
          if (gr.isValid(loc) && gr.get(loc) == null) {
            emptyLocs.add(loc);
          }
        }
      }
      if (emptyLocs.size() == 0) {
        return null;
      }
      final int r = World.generator.nextInt(emptyLocs.size());
      return emptyLocs.get(r);
    } else
    // unbounded grid
    {
      while (true) {
        // keep generating a random location until an empty one is found
        int r;
        if (rows < 0) {
          r = (int) (World.DEFAULT_ROWS * World.generator.nextGaussian());
        } else {
          r = World.generator.nextInt(rows);
        }
        int c;
        if (cols < 0) {
          c = (int) (World.DEFAULT_COLS * World.generator.nextGaussian());
        } else {
          c = World.generator.nextInt(cols);
        }
        final Location loc = new Location(r, c);
        if (gr.isValid(loc) && gr.get(loc) == null) {
          return loc;
        }
      }
    }
  }

  /**
   * Adds an occupant at a given location.
   * 
   * @param loc the location
   * @param occupant the occupant to add
   */
  public void add(final Location loc, final T occupant) {
    this.getGrid().put(loc, occupant);
    this.repaint();
  }

  /**
   * Removes an occupant from a given location.
   * 
   * @param loc the location
   * @return the removed occupant, or null if the location was empty
   */
  public T remove(final Location loc) {
    final T r = this.getGrid().remove(loc);
    this.repaint();
    return r;
  }

  /**
   * Adds a class to be shown in the "Set grid" menu.
   * 
   * @param className the name of the grid class
   */
  public void addGridClass(final String className) {
    this.gridClassNames.add(className);
  }

  /**
   * Adds a class to be shown when clicking on an empty location.
   * 
   * @param className the name of the occupant class
   */
  public void addOccupantClass(final String className) {
    this.occupantClassNames.add(className);
  }

  /**
   * Gets a set of grid classes that should be used by the world frame for this world.
   * 
   * @return the set of grid class names
   */
  public Set<String> getGridClasses() {
    return this.gridClassNames;
  }

  /**
   * Gets a set of occupant classes that should be used by the world frame for this world.
   * 
   * @return the set of occupant class names
   */
  public Set<String> getOccupantClasses() {
    return this.occupantClassNames;
  }

  private void repaint() {
    if (this.frame != null) {
      this.frame.repaint();
    }
  }

  /**
   * Returns a string that shows the positions of the grid occupants.
   */
  @Override
  public String toString() {
    String s = "";
    final Grid<?> gr = this.getGrid();
    int rmin = 0;
    int rmax = gr.getNumRows() - 1;
    int cmin = 0;
    int cmax = gr.getNumCols() - 1;
    if (rmax < 0 || cmax < 0) // unbounded grid
    {
      for (final Location loc : gr.getOccupiedLocations()) {
        final int r = loc.getRow();
        final int c = loc.getCol();
        if (r < rmin) {
          rmin = r;
        }
        if (r > rmax) {
          rmax = r;
        }
        if (c < cmin) {
          cmin = c;
        }
        if (c > cmax) {
          cmax = c;
        }
      }
    }
    for (int i = rmin; i <= rmax; i++) {
      for (int j = cmin; j < cmax; j++) {
        final Object obj = gr.get(new Location(i, j));
        if (obj == null) {
          s += " ";
        } else {
          s += obj.toString().substring(0, 1);
        }
      }
      s += "\n";
    }
    return s;
  }
}
