package info.gridworld.actor;

import java.util.function.BiConsumer;

import info.gridworld.actor.Shell.Tags;
import info.gridworld.actor.ShellWorld.Watchman;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.val;

public class ReportEvents {
  @Data
  @EqualsAndHashCode(callSuper = true)
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

    public static BiConsumer<Watchman, ReportEvent> impl() {
      return (final Watchman that, final ReportEvent r_) -> {
        val r = (CollisionReportEvent) r_;
        val collidedWith_ = r.getCollidedWith();
        if (!(collidedWith_ instanceof Shell)) {
          return;
        }
        val collidedWith = (Shell) collidedWith_;
        if ((boolean) collidedWith.getTags()
          .getOrDefault(Tags.PUSHABLE.getTag(), false) == true) {
          final int direction = r.getDirection();
          final Actor collider = r.getCollider();
          val grid = collider.getGrid();
          val destLoc = collidedWith.getLocation();
          val pushLoc = destLoc.getAdjacentLocation(direction);
          val displaced = grid.get(pushLoc);
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
