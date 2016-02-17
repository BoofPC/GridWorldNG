/*
 * AP(r) Computer Science GridWorld Case Study: Copyright(c) 2005-2006 Cay S. Horstmann
 * (http://horstmann.com) This code is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation. This
 * code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * @author Cay Horstmann
 */
package info.gridworld.grid;

import java.util.ArrayList;

/**
 * <code>AbstractGrid</code> contains the methods that are common to grid implementations. <br />
 * The implementation of this class is testable on the AP CS AB exam.
 */
public abstract class AbstractGrid<E> implements Grid<E> {
  @Override
  public ArrayList<E> getNeighbors(final Location loc) {
    final ArrayList<E> neighbors = new ArrayList<E>();
    for (final Location neighborLoc : this.getOccupiedAdjacentLocations(loc)) {
      neighbors.add(this.get(neighborLoc));
    }
    return neighbors;
  }

  @Override
  public ArrayList<Location> getValidAdjacentLocations(final Location loc) {
    final ArrayList<Location> locs = new ArrayList<Location>();
    int d = Location.NORTH;
    for (int i = 0; i < Location.FULL_CIRCLE / Location.HALF_RIGHT; i++) {
      final Location neighborLoc = loc.getAdjacentLocation(d);
      if (this.isValid(neighborLoc)) {
        locs.add(neighborLoc);
      }
      d = d + Location.HALF_RIGHT;
    }
    return locs;
  }

  @Override
  public ArrayList<Location> getEmptyAdjacentLocations(final Location loc) {
    final ArrayList<Location> locs = new ArrayList<Location>();
    for (final Location neighborLoc : this.getValidAdjacentLocations(loc)) {
      if (this.get(neighborLoc) == null) {
        locs.add(neighborLoc);
      }
    }
    return locs;
  }

  @Override
  public ArrayList<Location> getOccupiedAdjacentLocations(final Location loc) {
    final ArrayList<Location> locs = new ArrayList<Location>();
    for (final Location neighborLoc : this.getValidAdjacentLocations(loc)) {
      if (this.get(neighborLoc) != null) {
        locs.add(neighborLoc);
      }
    }
    return locs;
  }

  /**
   * Creates a string that describes this grid.
   * 
   * @return a string with descriptions of all objects in this grid (not necessarily in any
   *         particular order), in the format {loc=obj, loc=obj, ...}
   */
  @Override
  public String toString() {
    String s = "{";
    for (final Location loc : this.getOccupiedLocations()) {
      if (s.length() > 1) {
        s += ", ";
      }
      s += loc + "=" + this.get(loc);
    }
    return s + "}";
  }
}
