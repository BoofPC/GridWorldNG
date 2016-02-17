package info.gridworld.actor;

import java.awt.Color;

public class Actions {
  public static class MoveAction implements Action {
    private final int distance;

    @Override
    public String getType() {
      return "Move";
    }

    @Override
    public boolean isFinal() {
      return true;
    }

    public MoveAction(int distance) {
      this.distance = distance;
    }

    public int getDistance() {
      return distance;
    }
  }
  public static class TurnAction implements Action {
    private final int angle;

    @Override
    public String getType() {
      return "Turn";
    }

    @Override
    public boolean isFinal() {
      return false;
    }

    public TurnAction(int angle) {
      this.angle = angle;
    }

    public int getAngle() {
      return angle;
    }
  }
  public static class ColorAction implements Action {
    private final Color color;

    @Override
    public String getType() {
      return "Color";
    }

    @Override
    public boolean isFinal() {
      return false;
    }

    public ColorAction(Color color) {
      this.color = color;
    }

    public Color getColor() {
      return color;
    }
  }
}
