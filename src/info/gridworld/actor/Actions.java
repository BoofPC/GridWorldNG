package info.gridworld.actor;

import java.awt.Color;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.channels.Channels;
import java.nio.channels.Pipe;
import java.util.function.BiConsumer;
import java.util.function.Function;

import info.gridworld.actor.Util.Either;
import info.gridworld.grid.Location;
import javafx.util.Pair;
import lombok.Data;
import lombok.Value;
import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Actions {
  @Data
  public class MessageAction implements Action {
    /**
     * Either<shouting range, Either<polar offset, id> of recipient>
     */
    private final Either<Double, Either<Integer, Pair<Double, Double>>> recipient;
    private final Serializable message;

    @Override
    public boolean isFinal() {
      return false;
    }

    public static BiConsumer<Shell, Action> impl(final double maxDist) {
      return (final Shell that, final Action a) -> {
        val scope = ((MessageAction) a).getRecipient();
        val messageIn = ((MessageAction) a).getMessage();
        Serializable message_ = null;
        Pipe pipe_ = null;
        try {
          pipe_ = Pipe.open();
        } catch (IOException e) {
          e.printStackTrace();
        }
        val pipe = pipe_;
        try {
          new ObjectOutputStream(Channels.newOutputStream(pipe_.sink()))
            .writeObject(messageIn);
          try {
            message_ = (Serializable) new ObjectInputStream(
              Channels.newInputStream(pipe.source())).readObject();
          } catch (ClassNotFoundException e) {
            e.printStackTrace();
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
        val message = message_;
        val watchman = that.getWatchman();
        Function<Integer, ReportEvents.MessageReportEvent> report =
          id -> new ReportEvents.MessageReportEvent(that, that.getId(), id,
            message);
        if (scope.isRight()) {
          val recipient = scope.getRightValue();
          val grid = that.getGrid();
          if (recipient.isRight()) {
            // check and report MessageReportEvent at offset
            val offsetPolar = recipient.getRightValue();
            final double distance = Math.min(offsetPolar.getKey(), maxDist);
            final double direction = offsetPolar.getValue();
            val loc = that.getLocation();
            val offsets = Util.polarToRect(distance,
              Math.toRadians(direction + that.getDirection()));
            val targetLoc =
              new Location((int) (loc.getRow() + offsets.getKey()),
                (int) (loc.getCol() + offsets.getValue()));
            final Actor target_ = grid.get(targetLoc);
            if ((target_ == null) || !(target_ instanceof Shell)) {
              return;
            }
            final Shell target = (Shell) target_;
            watchman.report(report.apply(target.getId()));
          } else {
            val recipientId = recipient.getLeftValue();
            val recipientShell =
              (Shell) watchman.getWorld().getShells().get(recipientId);
            val loc = that.getLocation();
            val locRect = Util.locToRect(loc);
            val recipientLoc = recipientShell.getLocation();
            val recipientLocRect = Util.locToRect(recipientLoc);
            val offsetRect =
              Util.Pairs.thread(locRect, recipientLocRect, (x, y) -> x - y);
            val distance = Util.Pairs.apply(offsetRect, Math::hypot);
            if (distance > maxDist) {
              return;
            }
            watchman.report(report.apply(recipient.getLeftValue()));
          }
        } else {
          // shout MessageReportEvent to everybody in earshot
          val shoutRange = Math.min(scope.getLeftValue(), maxDist);
          val listeners = Util.actorsInRadius(that, shoutRange);
          listeners.filter(act -> act instanceof Shell).map(s -> (Shell) s)
            .map(Shell::getId).map(report).forEach(watchman::report);
        }
      };
    }
  }
  @Data
  public class MoveAction implements Action {
    private final int distance;

    @Override
    public boolean isFinal() {
      return true;
    }

    // TODO move to a better place
    public static Location sanitize(Location loc, int maxRow, int maxCol) {
      int row = loc.getRow();
      int col = loc.getCol();
      if (maxRow != -1) {
        row = Math.max(Math.min(row, maxRow), 0);
      }
      if (maxCol != -1) {
        col = Math.max(Math.min(col, maxCol), 0);
      }
      return new Location(row, col);
    }

    public static BiConsumer<Shell, Action> impl(final int maxDist) {
      return (final Shell that, final Action a) -> {
        final int distance = Math.min(((MoveAction) a).getDistance(), maxDist);
        final int direction = that.getDirection();
        val grid = that.getGrid();
        Location dest = that.getLocation();
        for (int i = distance; i > 0; i--) {
          dest = dest.getAdjacentLocation(direction);
        }
        dest = sanitize(dest, grid.getNumRows() - 1, grid.getNumCols() - 1);
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
  @Value
  public class TurnAction implements Action {
    private final int angle;

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
  public class ColorAction implements Action {
    private final Color color;

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
