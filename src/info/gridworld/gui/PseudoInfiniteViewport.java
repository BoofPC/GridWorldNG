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
import java.awt.Dimension;
import java.awt.Point;

import javax.swing.JScrollPane;
import javax.swing.JViewport;

/**
 * A <code>PseudoInfiniteViewport</code> is a <code>JViewport</code> subclass that translates scroll
 * actions into pan actions across an unbounded view. <br />
 * This code is not tested on the AP CS A and AB exams. It contains GUI implementation details that
 * are not intended to be understood by AP CS students.
 */
public class PseudoInfiniteViewport extends JViewport {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /**
   * The Pannable interface contains those methods the view installed in a PseudoInfiniteViewport
   * needs to support to enable panning behavior along with scrolling.
   */
  public interface Pannable {
    void panBy(int hDelta, int vDelta);

    boolean isPannableUnbounded();

    void showPanTip();
  }

  private final JScrollPane scrollParent;
  private Point panPoint = new Point(0, 0);

  /**
   * Construct a new PseudoInfiniteViewport object for the given scrollpane.
   * 
   * @param parent the JScrollPane for which this will be the viewport
   */
  public PseudoInfiniteViewport(final JScrollPane parent) {
    this.scrollParent = parent;
    this.setBackground(Color.lightGray);
  }

  /**
   * Sets the view position (upper left) to a new point. Overridden from JViewport to do a pan,
   * instead of scroll, on an unbounded view.
   * 
   * @param pt the Point to become the upper left
   */
  @Override
  public void setViewPosition(final Point pt) {
    final boolean isAdjusting =
      this.scrollParent.getVerticalScrollBar().getValueIsAdjusting()
        || this.scrollParent.getHorizontalScrollBar().getValueIsAdjusting();
    boolean changed = true;
    if (this.viewIsUnbounded()) {
      final int hDelta = pt.x - this.panPoint.x;
      final int vDelta = pt.y - this.panPoint.y;
      if (hDelta != 0 && vDelta == 0) {
        this.getPannableView().panBy(hDelta, vDelta);
      } else if (vDelta != 0 && hDelta == 0) {
        this.getPannableView().panBy(hDelta, vDelta);
      }
      else {
        changed = false; // no pan action was taken
      }
      this.panPoint = pt;
      if (!this.panPoint.equals(this.getPanCenterPoint()) && !isAdjusting) { // needs recentering
        this.panPoint = this.getPanCenterPoint();
        this.fireStateChanged(); // update scrollbars to match
      }
    } else
    // ordinary scroll behavior
    {
      changed = !this.getViewPosition().equals(pt);
      super.setViewPosition(pt);
    }
    if (changed || isAdjusting)
     {
      this.getPannableView().showPanTip(); // briefly show tip
    }
  }

  /**
   * Returns current view position (upper left). Overridden from JViewport to use pan center point
   * for unbounded view.
   */
  @Override
  public Point getViewPosition() {
    return (this.viewIsUnbounded() ? this.getPanCenterPoint() : super.getViewPosition());
  }

  /**
   * Returns current view size. Overridden from JViewport to use preferred virtual size for
   * unbounded view.
   */
  @Override
  public Dimension getViewSize() {
    return (this.viewIsUnbounded() ? this.getView().getPreferredSize()
      : super.getViewSize());
  }

  // some simple private helpers
  private Pannable getPannableView() {
    return (Pannable) this.getView();
  }

  private boolean viewIsUnbounded() {
    final Pannable p = this.getPannableView();
    return (p != null && p.isPannableUnbounded());
  }

  private Point getPanCenterPoint() {
    final Dimension size = this.getViewSize();
    return new Point(size.width / 2, size.height / 2);
  }
}
