/*
 * AP(r) Computer Science GridWorld Case Study: Copyright(c) 2005-2006 Cay S. Horstmann
 * (http://horstmann.com) This code is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation. This
 * code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * @author Cay Horstmann
 */
package info.gridworld.actor;

import java.awt.Color;

/**
 * A <code>Flower</code> is an actor that darkens over time. Some actors drop flowers as they move.
 * <br />
 * The API of this class is testable on the AP CS A and AB exams.
 */
public class Flower extends Actor {
  private static final Color DEFAULT_COLOR = Color.PINK;
  private static final double DARKENING_FACTOR = 0.05;

  // lose 5% of color value in each step
  /**
   * Constructs a pink flower.
   */
  public Flower() {
    this.setColor(Flower.DEFAULT_COLOR);
  }

  /**
   * Constructs a flower of a given color.
   * 
   * @param initialColor the initial color of this flower
   */
  public Flower(final Color initialColor) {
    this.setColor(initialColor);
  }

  /**
   * Causes the color of this flower to darken.
   */
  @Override
  public void act() {
    final Color c = this.getColor();
    final int red = (int) (c.getRed() * (1 - Flower.DARKENING_FACTOR));
    final int green = (int) (c.getGreen() * (1 - Flower.DARKENING_FACTOR));
    final int blue = (int) (c.getBlue() * (1 - Flower.DARKENING_FACTOR));
    this.setColor(new Color(red, green, blue));
  }
}
