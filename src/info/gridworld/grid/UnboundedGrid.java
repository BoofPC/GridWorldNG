/*
 * AP(r) Computer Science GridWorld Case Study: Copyright(c) 2002-2006 College Entrance Examination
 * Board (http://www.collegeboard.com). This code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation. This code is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU General Public License for more details.
 * @author Alyce Brady
 * @author APCS Development Committee
 * @author Cay Horstmann
 */
package info.gridworld.grid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * An <code>UnboundedGrid</code> is a rectangular grid with an unbounded number of rows and columns.
 * <br />
 * The implementation of this class is testable on the AP CS AB exam.
 */
public class UnboundedGrid<E> extends AbstractGrid<E> {
  private final Map<Location, E> occupantMap;

  /**
   * Constructs an empty unbounded grid.
   */
  public UnboundedGrid() {
    this.occupantMap = new HashMap<Location, E>();
  }

  @Override
  public int getNumRows() {
    return -1;
  }

  @Override
  public int getNumCols() {
    return -1;
  }

  @Override
  public boolean isValid(final Location loc) {
    return true;
  }

  @Override
  public ArrayList<Location> getOccupiedLocations() {
    final ArrayList<Location> a = new ArrayList<Location>();
    for (final Location loc : this.occupantMap.keySet()) {
      a.add(loc);
    }
    return a;
  }

  @Override
  public E get(final Location loc) {
    if (loc == null) {
      throw new NullPointerException("loc == null");
    }
    return this.occupantMap.get(loc);
  }

  @Override
  public E put(final Location loc, final E obj) {
    if (loc == null) {
      throw new NullPointerException("loc == null");
    }
    if (obj == null) {
      throw new NullPointerException("obj == null");
    }
    return this.occupantMap.put(loc, obj);
  }

  @Override
  public E remove(final Location loc) {
    if (loc == null) {
      throw new NullPointerException("loc == null");
    }
    return this.occupantMap.remove(loc);
  }
}
