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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javax.swing.JPanel;
import javax.swing.JToolTip;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.ToolTipManager;

import info.gridworld.grid.Grid;
import info.gridworld.grid.Location;

/**
 * A <code>GridPanel</code> is a panel containing a graphical display of the grid occupants. <br />
 * This code is not tested on the AP CS A and AB exams. It contains GUI implementation details that
 * are not intended to be understood by AP CS students.
 */
public class GridPanel extends JPanel
  implements Scrollable, PseudoInfiniteViewport.Pannable {
  private static final int MIN_CELL_SIZE = 12;
  private static final int DEFAULT_CELL_SIZE = 48;
  private static final int DEFAULT_CELL_COUNT = 10;
  private static final int TIP_DELAY = 1000;
  private Grid<?> grid;
  private int numRows, numCols, originRow, originCol;
  private int cellSize; // the size of each cell, EXCLUDING the gridlines
  private boolean toolTipsEnabled;
  private final Color backgroundColor = Color.WHITE;
  private final ResourceBundle resources;
  private final DisplayMap displayMap;
  private Location currentLocation;
  private Timer tipTimer;
  private JToolTip tip;
  private JPanel glassPane;

  /**
   * Construct a new GridPanel object with no grid. The view will be empty.
   */
  public GridPanel(final DisplayMap map, final ResourceBundle res) {
    this.displayMap = map;
    this.resources = res;
    this.setToolTipsEnabled(true);
  }

  /**
   * Paint this component.
   * 
   * @param g the Graphics object to use to render this component
   */
  @Override
  public void paintComponent(final Graphics g) {
    final Graphics2D g2 = (Graphics2D) g;
    super.paintComponent(g2);
    if (this.grid == null) {
      return;
    }
    final Insets insets = this.getInsets();
    g2.setColor(this.backgroundColor);
    g2.fillRect(insets.left, insets.top, this.numCols * (this.cellSize + 1) + 1,
      this.numRows * (this.cellSize + 1) + 1);
    this.drawWatermark(g2);
    this.drawGridlines(g2);
    this.drawOccupants(g2);
    this.drawCurrentLocation(g2);
  }

  /**
   * Draw one occupant object. First verify that the object is actually visible before any drawing,
   * set up the clip appropriately and use the DisplayMap to determine which object to call upon to
   * render this particular Locatable. Note that we save and restore the graphics transform to
   * restore back to normalcy no matter what the renderer did to to the coordinate system.
   * 
   * @param g2 the Graphics2D object to use to render
   * @param xleft the leftmost pixel of the rectangle
   * @param ytop the topmost pixel of the rectangle
   * @param obj the Locatable object to draw
   */
  private void drawOccupant(final Graphics2D g2, final int xleft,
    final int ytop, final Object obj) {
    final Rectangle cellToDraw =
      new Rectangle(xleft, ytop, this.cellSize, this.cellSize);
    // Only draw if the object is visible within the current clipping
    // region.
    if (cellToDraw.intersects(g2.getClip().getBounds())) {
      final Graphics2D g2copy = (Graphics2D) g2.create();
      g2copy.clip(cellToDraw);
      // Get the drawing object to display this occupant.
      final Display displayObj = this.displayMap.findDisplayFor(obj.getClass());
      displayObj.draw(obj, this, g2copy, cellToDraw);
      g2copy.dispose();
    }
  }

  /**
   * Draw the gridlines for the grid. We only draw the portion of the lines that intersect the
   * current clipping bounds.
   * 
   * @param g2 the Graphics2 object to use to render
   */
  private void drawGridlines(final Graphics2D g2) {
    final Rectangle curClip = g2.getClip().getBounds();
    final int top = this.getInsets().top, left = this.getInsets().left;
    final int miny =
      Math.max(0, (curClip.y - top) / (this.cellSize + 1)) * (this.cellSize + 1)
        + top;
    final int minx = Math.max(0, (curClip.x - left) / (this.cellSize + 1))
      * (this.cellSize + 1) + left;
    final int maxy = Math.min(this.numRows,
      (curClip.y + curClip.height - top + this.cellSize) / (this.cellSize + 1))
      * (this.cellSize + 1) + top;
    final int maxx = Math.min(this.numCols,
      (curClip.x + curClip.width - left + this.cellSize) / (this.cellSize + 1))
      * (this.cellSize + 1) + left;
    g2.setColor(Color.GRAY);
    for (int y = miny; y <= maxy; y += this.cellSize + 1) {
      for (int x = minx; x <= maxx; x += this.cellSize + 1) {
        final Location loc = this.locationForPoint(
          new Point(x + this.cellSize / 2, y + this.cellSize / 2));
        if (loc != null && !this.grid.isValid(loc)) {
          g2.fillRect(x + 1, y + 1, this.cellSize, this.cellSize);
        }
      }
    }
    g2.setColor(Color.BLACK);
    for (int y = miny; y <= maxy; y += this.cellSize + 1) {
      // draw horizontal lines
      g2.drawLine(minx, y, maxx, y);
    }
    for (int x = minx; x <= maxx; x += this.cellSize + 1) {
      // draw vertical lines
      g2.drawLine(x, miny, x, maxy);
    }
  }

  /**
   * Draws the occupants of the grid.
   * 
   * @param g2 the graphics context
   */
  private void drawOccupants(final Graphics2D g2) {
    final ArrayList<Location> occupantLocs = this.grid.getOccupiedLocations();
    for (int index = 0; index < occupantLocs.size(); index++) {
      final Location loc = occupantLocs.get(index);
      final int xleft = this.colToXCoord(loc.getCol());
      final int ytop = this.rowToYCoord(loc.getRow());
      this.drawOccupant(g2, xleft, ytop, this.grid.get(loc));
    }
  }

  /**
   * Draws a square that marks the current location.
   * 
   * @param g2 the graphics context
   */
  private void drawCurrentLocation(final Graphics2D g2) {
    if ("hide".equals(System.getProperty("info.gridworld.gui.selection"))) {
      return;
    }
    if (this.currentLocation != null) {
      final Point p = this.pointForLocation(this.currentLocation);
      g2.drawRect(p.x - this.cellSize / 2 - 2, p.y - this.cellSize / 2 - 2,
        this.cellSize + 3, this.cellSize + 3);
    }
  }

  /**
   * Draws a watermark that shows the version number if it is < 1.0
   * 
   * @param g2 the graphics context
   */
  private void drawWatermark(Graphics2D g2) {
    if ("hide".equals(System.getProperty("info.gridworld.gui.watermark"))) {
      return;
    }
    g2 = (Graphics2D) g2.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
      RenderingHints.VALUE_ANTIALIAS_ON);
    final Rectangle rect = this.getBounds();
    g2.setPaint(new Color(0xE3, 0xD3, 0xD3));
    final int WATERMARK_FONT_SIZE = 100;
    final String s = this.resources.getString("version.id");
    if ("1.0".compareTo(s) <= 0) {
      return;
    }
    g2.setFont(new Font("SansSerif", Font.BOLD, WATERMARK_FONT_SIZE));
    final FontRenderContext frc = g2.getFontRenderContext();
    final Rectangle2D bounds = g2.getFont().getStringBounds(s, frc);
    final float centerX = rect.x + rect.width / 2;
    final float centerY = rect.y + rect.height / 2;
    final float leftX = centerX - (float) bounds.getWidth() / 2;
    final LineMetrics lm = g2.getFont().getLineMetrics(s, frc);
    final float baselineY = centerY - lm.getHeight() / 2 + lm.getAscent();
    g2.drawString(s, leftX, baselineY);
  }

  /**
   * Enables/disables showing of tooltip giving information about the occupant beneath the mouse.
   * 
   * @param flag true/false to enable/disable tool tips
   */
  public void setToolTipsEnabled(boolean flag) {
    if ("hide".equals(System.getProperty("info.gridworld.gui.tooltips"))) {
      flag = false;
    }
    if (flag) {
      ToolTipManager.sharedInstance().registerComponent(this);
    } else {
      ToolTipManager.sharedInstance().unregisterComponent(this);
    }
    this.toolTipsEnabled = flag;
  }

  /**
   * Sets the grid being displayed. Reset the cellSize to be the largest that fits the entire grid
   * in the current visible area (use default if grid is too large).
   * 
   * @param gr the grid to display
   */
  public void setGrid(final Grid<?> gr) {
    this.currentLocation = new Location(0, 0);
    final JViewport vp = this.getEnclosingViewport(); // before changing, reset
    // scroll/pan position
    if (vp != null) {
      vp.setViewPosition(new Point(0, 0));
    }
    this.grid = gr;
    this.originRow = this.originCol = 0;
    if (this.grid.getNumRows() == -1 && this.grid.getNumCols() == -1) {
      this.numRows = this.numCols = 2000;
      // This determines the "virtual" size of the pan world
    } else {
      this.numRows = this.grid.getNumRows();
      this.numCols = this.grid.getNumCols();
    }
    this.recalculateCellSize(GridPanel.MIN_CELL_SIZE);
  }

  // private helpers to calculate extra width/height needs for borders/insets.
  private int extraWidth() {
    return this.getInsets().left + this.getInsets().right;
  }

  private int extraHeight() {
    return this.getInsets().top + this.getInsets().left;
  }

  /**
   * Returns the desired size of the display, for use by layout manager.
   * 
   * @return preferred size
   */
  @Override
  public Dimension getPreferredSize() {
    return new Dimension(
      this.numCols * (this.cellSize + 1) + 1 + this.extraWidth(),
      this.numRows * (this.cellSize + 1) + 1 + this.extraHeight());
  }

  /**
   * Returns the minimum size of the display, for use by layout manager.
   * 
   * @return minimum size
   */
  @Override
  public Dimension getMinimumSize() {
    return new Dimension(
      this.numCols * (GridPanel.MIN_CELL_SIZE + 1) + 1 + this.extraWidth(),
      this.numRows * (GridPanel.MIN_CELL_SIZE + 1) + 1 + this.extraHeight());
  }

  /**
   * Zooms in the display by doubling the current cell size.
   */
  public void zoomIn() {
    this.cellSize *= 2;
    this.revalidate();
  }

  /**
   * Zooms out the display by halving the current cell size.
   */
  public void zoomOut() {
    this.cellSize = Math.max(this.cellSize / 2, GridPanel.MIN_CELL_SIZE);
    this.revalidate();
  }

  /**
   * Pans the display back to the origin, so that 0, 0 is at the the upper left of the visible
   * viewport.
   */
  public void recenter(final Location loc) {
    this.originRow = loc.getRow();
    this.originCol = loc.getCol();
    this.repaint();
    final JViewport vp = this.getEnclosingViewport();
    if (vp != null) {
      if (!this.isPannableUnbounded()
        || !(vp instanceof PseudoInfiniteViewport)) {
        vp.setViewPosition(this.pointForLocation(loc));
      } else {
        this.showPanTip();
      }
    }
  }

  /**
   * Given a Point determine which grid location (if any) is under the mouse. This method is used by
   * the GUI when creating Fish by clicking on cells in the display.
   * 
   * @param p the Point in question (in display's coordinate system)
   * @return the Location beneath the event (which may not be a valid location in the grid)
   */
  public Location locationForPoint(final Point p) {
    return new Location(this.yCoordToRow(p.y), this.xCoordToCol(p.x));
  }

  public Point pointForLocation(final Location loc) {
    return new Point(this.colToXCoord(loc.getCol()) + this.cellSize / 2,
      this.rowToYCoord(loc.getRow()) + this.cellSize / 2);
  }

  // private helpers to convert between (x,y) and (row,col)
  private int xCoordToCol(final int xCoord) {
    return (xCoord - 1 - this.getInsets().left) / (this.cellSize + 1)
      + this.originCol;
  }

  private int yCoordToRow(final int yCoord) {
    return (yCoord - 1 - this.getInsets().top) / (this.cellSize + 1)
      + this.originRow;
  }

  private int colToXCoord(final int col) {
    return (col - this.originCol) * (this.cellSize + 1) + 1
      + this.getInsets().left;
  }

  private int rowToYCoord(final int row) {
    return (row - this.originRow) * (this.cellSize + 1) + 1
      + this.getInsets().top;
  }

  /**
   * Given a MouseEvent, determine what text to place in the floating tool tip when the the mouse
   * hovers over this location. If the mouse is over a valid grid cell. we provide some information
   * about the cell and its contents. This method is automatically called on mouse-moved events
   * since we register for tool tips.
   * 
   * @param evt the MouseEvent in question
   * @return the tool tip string for this location
   */
  @Override
  public String getToolTipText(final MouseEvent evt) {
    final Location loc = this.locationForPoint(evt.getPoint());
    return this.getToolTipText(loc);
  }

  private String getToolTipText(final Location loc) {
    if (!this.toolTipsEnabled || loc == null || !this.grid.isValid(loc)) {
      return null;
    }
    final Object f = this.grid.get(loc);
    if (f != null) {
      return MessageFormat.format(
        this.resources.getString("cell.tooltip.nonempty"),
        new Object[] {loc, f});
    } else {
      return MessageFormat.format(
        this.resources.getString("cell.tooltip.empty"), new Object[] {loc, f});
    }
  }

  /**
   * Sets the current location.
   * 
   * @param loc the new location
   */
  public void setCurrentLocation(final Location loc) {
    this.currentLocation = loc;
  }

  /**
   * Gets the current location.
   * 
   * @return the currently selected location (marked with a bold square)
   */
  public Location getCurrentLocation() {
    return this.currentLocation;
  }

  /**
   * Moves the current location by a given amount.
   * 
   * @param dr the number of rows by which to move the location
   * @param dc the number of columns by which to move the location
   */
  public void moveLocation(final int dr, final int dc) {
    final Location newLocation = new Location(
      this.currentLocation.getRow() + dr, this.currentLocation.getCol() + dc);
    if (!this.grid.isValid(newLocation)) {
      return;
    }
    this.currentLocation = newLocation;
    final JViewport viewPort = this.getEnclosingViewport();
    if (this.isPannableUnbounded()) {
      if (this.originRow > this.currentLocation.getRow()) {
        this.originRow = this.currentLocation.getRow();
      }
      if (this.originCol > this.currentLocation.getCol()) {
        this.originCol = this.currentLocation.getCol();
      }
      final Dimension dim = viewPort.getSize();
      final int rows = dim.height / (this.cellSize + 1);
      final int cols = dim.width / (this.cellSize + 1);
      if (this.originRow + rows - 1 < this.currentLocation.getRow()) {
        this.originRow = this.currentLocation.getRow() - rows + 1;
      }
      if (this.originCol + rows - 1 < this.currentLocation.getCol()) {
        this.originCol = this.currentLocation.getCol() - cols + 1;
      }
    } else if (viewPort != null) {
      int dx = 0;
      int dy = 0;
      final Point p = this.pointForLocation(this.currentLocation);
      final Rectangle locRect = new Rectangle(p.x - this.cellSize / 2,
        p.y - this.cellSize / 2, this.cellSize + 1, this.cellSize + 1);
      final Rectangle viewRect = viewPort.getViewRect();
      if (!viewRect.contains(locRect)) {
        while (locRect.x < viewRect.x + dx) {
          dx -= this.cellSize + 1;
        }
        while (locRect.y < viewRect.y + dy) {
          dy -= this.cellSize + 1;
        }
        while (locRect.getMaxX() > viewRect.getMaxX() + dx) {
          dx += this.cellSize + 1;
        }
        while (locRect.getMaxY() > viewRect.getMaxY() + dy) {
          dy += this.cellSize + 1;
        }
        final Point pt = viewPort.getViewPosition();
        pt.x += dx;
        pt.y += dy;
        viewPort.setViewPosition(pt);
      }
    }
    this.repaint();
    this.showTip(this.getToolTipText(this.currentLocation),
      this.pointForLocation(this.currentLocation));
  }

  /**
   * Show a tool tip.
   * 
   * @param tipText the tool tip text
   * @param pt the pixel position over which to show the tip
   */
  public void showTip(final String tipText, final Point pt) {
    if (this.getRootPane() == null) {
      return;
    }
    // draw in glass pane to appear on top of other components
    if (this.glassPane == null) {
      this.getRootPane().setGlassPane(this.glassPane = new JPanel());
      this.glassPane.setOpaque(false);
      this.glassPane.setLayout(null); // will control layout manually
      this.glassPane.add(this.tip = new JToolTip());
      this.tipTimer = new Timer(GridPanel.TIP_DELAY,
        evt -> GridPanel.this.glassPane.setVisible(false));
      this.tipTimer.setRepeats(false);
    }
    if (tipText == null) {
      return;
    }
    // set tip text to identify current origin of pannable view
    this.tip.setTipText(tipText);
    // position tip to appear at upper left corner of viewport
    this.tip.setLocation(SwingUtilities.convertPoint(this, pt, this.glassPane));
    this.tip.setSize(this.tip.getPreferredSize());
    // show glass pane (it contains tip)
    this.glassPane.setVisible(true);
    this.glassPane.repaint();
    // this timer will hide the glass pane after a short delay
    this.tipTimer.restart();
  }

  /**
   * Calculate the cell size to use given the current viewable region and the the number of rows and
   * columns in the grid. We use the largest cellSize that will fit in the viewable region, bounded
   * to be at least the parameter minSize.
   */
  private void recalculateCellSize(final int minSize) {
    if (this.numRows == 0 || this.numCols == 0) {
      this.cellSize = 0;
    } else {
      final JViewport vp = this.getEnclosingViewport();
      final Dimension viewableSize =
        (vp != null) ? vp.getSize() : this.getSize();
      final int desiredCellSize =
        Math.min((viewableSize.height - this.extraHeight()) / this.numRows,
          (viewableSize.width - this.extraWidth()) / this.numCols) - 1;
      // now we want to approximate this with 
      // DEFAULT_CELL_SIZE * Math.pow(2, k)
      this.cellSize = GridPanel.DEFAULT_CELL_SIZE;
      if (this.cellSize <= desiredCellSize) {
        while (2 * this.cellSize <= desiredCellSize) {
          this.cellSize *= 2;
        }
      } else {
        while (this.cellSize / 2 >= Math.max(desiredCellSize,
          GridPanel.MIN_CELL_SIZE)) {
          this.cellSize /= 2;
        }
      }
    }
    this.revalidate();
  }

  // helper to get our parent viewport, if we are in one.
  private JViewport getEnclosingViewport() {
    final Component parent = this.getParent();
    return (parent instanceof JViewport) ? (JViewport) parent : null;
  }

  // GridPanel implements the Scrollable interface to get nicer behavior in a
  // JScrollPane. The 5 methods below are the methods in that interface
  @Override
  public int getScrollableUnitIncrement(final Rectangle visibleRect,
    final int orientation, final int direction) {
    return this.cellSize + 1;
  }

  @Override
  public int getScrollableBlockIncrement(final Rectangle visibleRect,
    final int orientation, final int direction) {
    if (orientation == SwingConstants.VERTICAL) {
      return (int) (visibleRect.height * .9);
    } else {
      return (int) (visibleRect.width * .9);
    }
  }

  @Override
  public boolean getScrollableTracksViewportWidth() {
    return false;
  }

  @Override
  public boolean getScrollableTracksViewportHeight() {
    return false;
  }

  @Override
  public Dimension getPreferredScrollableViewportSize() {
    return new Dimension(
      GridPanel.DEFAULT_CELL_COUNT * (GridPanel.DEFAULT_CELL_SIZE + 1) + 1
        + this.extraWidth(),
      GridPanel.DEFAULT_CELL_COUNT * (GridPanel.DEFAULT_CELL_SIZE + 1) + 1
        + this.extraHeight());
  }

  // GridPanel implements the PseudoInfiniteViewport.Pannable interface to
  // play nicely with the pan behavior for unbounded view.
  // The 3 methods below are the methods in that interface.
  @Override
  public void panBy(final int hDelta, final int vDelta) {
    this.originCol += hDelta / (this.cellSize + 1);
    this.originRow += vDelta / (this.cellSize + 1);
    this.repaint();
  }

  @Override
  public boolean isPannableUnbounded() {
    return this.grid != null
      && (this.grid.getNumRows() == -1 || this.grid.getNumCols() == -1);
  }

  /**
   * Shows a tool tip over the upper left corner of the viewport with the contents of the pannable
   * view's pannable tip text (typically a string identifiying the corner point). Tip is removed
   * after a short delay.
   */
  @Override
  public void showPanTip() {
    String tipText = null;
    Point upperLeft = new Point(0, 0);
    final JViewport vp = this.getEnclosingViewport();
    if (!this.isPannableUnbounded() && vp != null) {
      upperLeft = vp.getViewPosition();
    }
    final Location loc = this.locationForPoint(upperLeft);
    if (loc != null) {
      tipText = this.getToolTipText(loc);
    }
    this.showTip(tipText, this.getLocation());
  }
}
