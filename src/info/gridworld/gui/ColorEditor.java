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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.beans.PropertyEditorSupport;

import javax.swing.Icon;
import javax.swing.JComboBox;

/**
 * A property editor for the Color type. <br />
 * This code is not tested on the AP CS A and AB exams. It contains GUI implementation details that
 * are not intended to be understood by AP CS students.
 */
public class ColorEditor extends PropertyEditorSupport {
  public ColorEditor() {
    this.combo = new JComboBox(ColorEditor.colorIcons);
  }

  @Override
  public Object getValue() {
    final ColorIcon value = (ColorIcon) this.combo.getSelectedItem();
    return value.getColor();
  }

  @Override
  public boolean supportsCustomEditor() {
    return true;
  }

  @Override
  public Component getCustomEditor() {
    this.combo.setSelectedItem(0);
    return this.combo;
  }

  private interface ColorIcon extends Icon {
    Color getColor();

    int WIDTH = 120;
    int HEIGHT = 20;
  }
  private static class SolidColorIcon implements ColorIcon {
    private final Color color;

    @Override
    public Color getColor() {
      return this.color;
    }

    public SolidColorIcon(final Color c) {
      this.color = c;
    }

    @Override
    public int getIconWidth() {
      return ColorIcon.WIDTH;
    }

    @Override
    public int getIconHeight() {
      return ColorIcon.HEIGHT;
    }

    @Override
    public void paintIcon(final Component c, final Graphics g, final int x,
      final int y) {
      final Rectangle r =
        new Rectangle(x, y, ColorIcon.WIDTH - 1, ColorIcon.HEIGHT - 1);
      final Graphics2D g2 = (Graphics2D) g;
      final Color oldColor = g2.getColor();
      g2.setColor(this.color);
      g2.fill(r);
      g2.setColor(Color.BLACK);
      g2.draw(r);
      g2.setColor(oldColor);
    }
  }
  private static class RandomColorIcon implements ColorIcon {
    @Override
    public Color getColor() {
      return new Color((int) (Math.random() * 256 * 256 * 256));
    }

    @Override
    public int getIconWidth() {
      return ColorIcon.WIDTH;
    }

    @Override
    public int getIconHeight() {
      return ColorIcon.HEIGHT;
    }

    @Override
    public void paintIcon(final Component c, final Graphics g, final int x,
      final int y) {
      final Rectangle r =
        new Rectangle(x, y, ColorIcon.WIDTH - 1, ColorIcon.HEIGHT - 1);
      final Graphics2D g2 = (Graphics2D) g;
      final Color oldColor = g2.getColor();
      final Rectangle r1 =
        new Rectangle(x, y, ColorIcon.WIDTH / 4, ColorIcon.HEIGHT - 1);
      for (int i = 0; i < 4; i++) {
        g2.setColor(this.getColor());
        g2.fill(r1);
        r1.translate(ColorIcon.WIDTH / 4, 0);
      }
      g2.setColor(Color.BLACK);
      g2.draw(r);
      g2.setColor(oldColor);
    }
  }

  private final JComboBox combo;
  private static Color[] colorValues = {Color.BLACK, Color.BLUE, Color.CYAN,
      Color.DARK_GRAY, Color.GRAY, Color.GREEN, Color.LIGHT_GRAY, Color.MAGENTA,
      Color.ORANGE, Color.PINK, Color.RED, Color.WHITE, Color.YELLOW};
  private static ColorIcon[] colorIcons;

  static {
    ColorEditor.colorIcons = new ColorIcon[ColorEditor.colorValues.length + 1];
    ColorEditor.colorIcons[0] = new RandomColorIcon();
    for (int i = 0; i < ColorEditor.colorValues.length; i++) {
      ColorEditor.colorIcons[i + 1] =
        new SolidColorIcon(ColorEditor.colorValues[i]);
    }
  }
}
