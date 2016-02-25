package info.gridworld.actor;

import java.awt.Color;
import java.util.function.BiConsumer;

import info.gridworld.grid.Location;
import lombok.Data;

public class Actions {
  @Data
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

    public static BiConsumer<Shell, Action> impl(final int maxDist) {
      return (final Shell that, final Action a) -> {
        final int distance = Math.min(((MoveAction) a).getDistance(), maxDist);
        final int direction = that.getDirection();
        final Location destination = that.getLocation();
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
  @Data
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

    public static BiConsumer<Shell, Action> impl() {
      return (final Shell that, final Action a) -> {
        final int angle = ((TurnAction) a).getAngle();
        final int direction = that.getDirection();
        that.setDirection((direction + angle * 45) % 360);
      };
    }
  }
  @Data
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

    public static BiConsumer<Shell, Action> impl() {
      return (final Shell that, final Action a) -> {
        final Color color = ((ColorAction) a).getColor();
        that.setColor(color);
      };
    }
  }
}
