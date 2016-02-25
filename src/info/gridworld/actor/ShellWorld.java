package info.gridworld.actor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import info.gridworld.grid.Grid;
import info.gridworld.grid.Location;
import lombok.Getter;

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
    final Grid<Actor> gr = this.getGrid();
    final ArrayList<Actor> actors = new ArrayList<Actor>();
    for (final Location loc : gr.getOccupiedLocations()) {
      actors.add(gr.get(loc));
    }
    for (final Actor a : actors) {
      // only act if another actor hasn't removed a
      if (a.getGrid() == gr) {
        if (a instanceof Shell) {
          ((Shell) a)
            .respond(new ActorEvents.StepEvent("I see what you did there"));
        }
        a.act();
      }
    }
  }
}
