package info.gridworld.actor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import info.gridworld.grid.Grid;
import info.gridworld.grid.Location;
import lombok.Getter;
import lombok.NonNull;
import lombok.val;

@Getter
public class ShellWorld extends ActorWorld {
  public static class Watchman implements ReportListener {
    @Getter private final @NonNull ShellWorld world;
    private final Map<Class<? extends ReportEvent>, BiConsumer<Watchman, ReportEvent>> reportImpls =
      new HashMap<>();

    public Watchman(final @NonNull ShellWorld world) {
      this.world = world;
      this.reportImpls.put(ReportEvents.CollisionReportEvent.class,
        ReportEvents.CollisionReportEvent.impl());
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

  private final @NonNull Watchman watchman = new Watchman(this);
  private final @NonNull Map<Integer, Shell> shells = new HashMap<>();

  @Override
  public void add(Location loc, Actor occupant) {
    if (occupant instanceof Shell) {
      val shell = (Shell) occupant;
      this.shells.put(shell.getId(), shell);
    }
    super.add(loc, occupant);
  }

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
