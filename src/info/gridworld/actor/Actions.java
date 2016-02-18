package info.gridworld.actor;

import java.awt.Color;
import java.util.function.BiConsumer;

import info.gridworld.grid.Location;

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

    public static BiConsumer<Shell, Action> impl(int maxDist) {
      return (Shell that, Action a) -> {
        final int distance = Math.min(((MoveAction) a).getDistance(), maxDist);
        final int direction = that.getDirection();
        Location destination = that.getLocation();
        for (int i = distance; i > 0; i--) {
          destination.getAdjacentLocation(direction);
        }
        final Actor destActor = that.getGrid().get(destination);
        if (destActor != null) {
          that.getWatchman().report(new ReportEvents.CollisionReportEvent(that,
            that, destActor, direction));
          return;
        }
        that.moveTo(destination);
      };
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

    public static BiConsumer<Shell, Action> impl() {
      return (Shell that, Action a) -> {
        final int angle = ((TurnAction) a).getAngle();
        final int direction = that.getDirection();
        that.setDirection((direction + angle * 45) % 360);
      };
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

    public static BiConsumer<Shell, Action> impl() {
      return (Shell that, Action a) -> {
        final Color color = ((ColorAction) a).getColor();
        that.setColor(color);
      };
    }
  }
}
