package info.gridworld.actor;

import java.awt.Color;
import java.util.function.BiConsumer;

import info.gridworld.grid.Location;
import lombok.Data;
import lombok.val;

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
        val grid = that.getGrid();
        val maxRow = grid.getNumRows() - 1;
        val maxCol = grid.getNumCols() - 1;
        Location dest = that.getLocation();
        for (int i = distance; i > 0; i--) {
          dest = dest.getAdjacentLocation(direction);
        }
        {
          int row = dest.getRow();
          int col = dest.getCol();
          if (maxRow != -1) {
            row = Math.max(Math.min(row, maxRow), 0);
          }
          if (maxCol != -1) {
            col = Math.max(Math.min(col, maxCol), 0);
          }
          dest = new Location(row, col);
        }
        val destActor = grid.get(dest);
        if (destActor != null) {
          that.getWatchman().report(new ReportEvents.CollisionReportEvent(that,
            that, destActor, direction));
          return;
        }
        that.moveTo(dest);
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
        val color = ((ColorAction) a).getColor();
        that.setColor(color);
      };
    }
  }
}
