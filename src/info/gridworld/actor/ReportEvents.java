package info.gridworld.actor;

import java.util.function.BiConsumer;

import info.gridworld.actor.Shell.Tags;
import info.gridworld.actor.ShellWorld.Watchman;
import info.gridworld.grid.Grid;
import info.gridworld.grid.Location;

public class ReportEvents {
  public static class CollisionReportEvent extends ReportEvent {
    private static final long serialVersionUID = 1L;
    private final Actor collider;
    private final Actor collidedWith;
    private final int direction;

    public CollisionReportEvent(final Object source, final Actor collider,
      final Actor collidedWith, final int direction) {
      super(source);
      this.collider = collider;
      this.collidedWith = collidedWith;
      this.direction = direction;
    }

    public Actor getCollider() {
      return collider;
    }

    public Actor getCollidedWith() {
      return collidedWith;
    }

    public int getDirection() {
      return direction;
    }

    public static BiConsumer<Watchman, ReportEvent> impl() {
      return (Watchman that, ReportEvent r_) -> {
        final CollisionReportEvent r = (CollisionReportEvent) r_;
        final Actor collidedWith_ = r.getCollidedWith();
        if (!(collidedWith_ instanceof Shell)) {
          return;
        }
        final Shell collidedWith = (Shell) collidedWith_;
        if (collidedWith.getTags().contains(Tags.PUSHABLE.get())) {
          final int direction = r.getDirection();
          final Actor collider = r.getCollider();
          final Grid<Actor> grid = collider.getGrid();
          final Location destLoc = collidedWith.getLocation();
          final Location pushLoc = destLoc.getAdjacentLocation(direction);
          final Actor displaced = grid.get(pushLoc);
          if (displaced != null) {
            that.report(new CollisionReportEvent(that, collidedWith, displaced,
              direction));
            return;
          }
          collidedWith.moveTo(pushLoc);
          collider.moveTo(destLoc);
        }
      };
    }
  }
}
