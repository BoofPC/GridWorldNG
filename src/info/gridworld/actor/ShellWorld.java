package info.gridworld.actor;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class ShellWorld extends ActorWorld {
  public static class Watchman implements ReportListener {
    private final ActorWorld world;
    private final Map<Class<? extends ReportEvent>, BiConsumer<Watchman, ReportEvent>> reportImpls =
      new HashMap<>();

    public Watchman(final ActorWorld world) {
      this.world = world;
      reportImpls.put(ReportEvents.CollisionReportEvent.class,
        ReportEvents.CollisionReportEvent.impl());
    }

    public ActorWorld getWorld() {
      return world;
    }

    public void report(ReportEvent r) {
      final Class<? extends ReportEvent> clazz = r.getClass();
      final BiConsumer<Watchman, ReportEvent> impl =
        this.reportImpls.get(clazz);
      if (impl != null) {
        impl.accept(this, r);
      }
    }
  }

  private Watchman watchman = new Watchman(this);

  public Watchman getWatchman() {
    return watchman;
  }
}
