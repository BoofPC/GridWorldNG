package info.gridworld.actor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import info.gridworld.grid.Grid;
import lombok.Getter;
import lombok.val;

@Getter
public class ShellWorld extends ActorWorld {
  public static class Watchman implements ReportListener {
    private final ActorWorld world;
    private final Map<Class<? extends ReportEvent>, BiConsumer<Watchman, ReportEvent>> reportImpls =
      new HashMap<>();

    public Watchman(final ActorWorld world) {
      this.world = world;
      this.reportImpls.put(ReportEvents.CollisionReportEvent.class,
        ReportEvents.CollisionReportEvent.impl());
    }

    public ActorWorld getWorld() {
      return this.world;
    }

    @Override
    public void report(final ReportEvent r) {
      final Class<? extends ReportEvent> clazz = r.getClass();
      final BiConsumer<Watchman, ReportEvent> impl =
        this.reportImpls.get(clazz);
      if (impl != null) {
        impl.accept(this, r);
      }
    }
  }

  private final Watchman watchman = new Watchman(this);

  @Override
  public void step() {
    val grid = this.getGrid();
    final List<Actor> actors = new ArrayList<>();
    for (val loc : grid.getOccupiedLocations()) {
      actors.add(grid.get(loc));
    }
    for (val actor : actors) {
      // only act if another actor hasn't removed actor
      if (actor.getGrid() == grid) {
        if (actor instanceof Shell) {
          ((Shell) actor)
            .respond(new ActorEvents.StepEvent("I see what you did there"));
        }
        actor.act();
      }
    }
  }

  public ShellWorld(Grid<Actor> grid) {
    super(grid);
  }

  public ShellWorld() {
    super();
  }
}
