package info.gridworld.actor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import info.gridworld.actor.Actions.ColorAction;
import info.gridworld.actor.Actions.MoveAction;
import info.gridworld.actor.Actions.TurnAction;
import info.gridworld.actor.ActorEvent.ActorInfo;
import info.gridworld.actor.ShellWorld.Watchman;
import info.gridworld.grid.Grid;
import info.gridworld.grid.Location;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class Shell extends Actor {
  @Getter
  @RequiredArgsConstructor
  public enum Tags implements info.gridworld.actor.Tags {
    PUSHABLE("Shell.Pushable", (Boolean) true);
    private final String tag;
    private final Object initial;
  }

  private final int id;
  private final ActorListener brain;
  private final Watchman watchman;
  private Iterable<Action> nextActions;
  private final Map<Class<? extends Action>, BiConsumer<Shell, Action>> actionImpls =
    new HashMap<>();
  private final Map<String, Object> tags = new HashMap<>();

  public Shell(final int id, final ActorListener brain,
    final Watchman watchman) {
    this.id = id;
    this.brain = brain;
    this.watchman = watchman;
    this.actionImpls.put(MoveAction.class, MoveAction.impl(1));
    this.actionImpls.put(TurnAction.class, TurnAction.impl());
    this.actionImpls.put(ColorAction.class, ColorAction.impl());
  }

  public int getId() {
    return id;
  }

  public void respond(final ActorEvent event) {
    final ActorInfo that = ActorInfo.builder().id(this.id).distance(0)
      .color(this.getColor()).build();
    final Set<ActorInfo> environment = new HashSet<>();
    final Grid<Actor> grid = this.getGrid();
    final Location myLoc = this.getLocation();
    final int myRow = myLoc.getRow();
    final int myCol = myLoc.getCol();
    // wheeee, square radii
    final int sightRadius = 3;
    for (int row = myRow - sightRadius; row < myRow + sightRadius; row++) {
      for (int col = myCol - sightRadius; col < myCol + sightRadius; col++) {
        final Location loc = new Location(row, col);
        final Actor actor = grid.get(loc);
        if (actor == null) {
          continue;
        }
        final ActorInfo.ActorInfoBuilder actorInfo = ActorInfo.builder();
        final Location actorLoc = actor.getLocation();
        final int actorRow = actorLoc.getRow();
        final int actorCol = actorLoc.getCol();
        final int dRow = actorRow - myRow;
        final int dCol = actorCol - myCol;
        actorInfo.direction((int) Math.toDegrees(Math.atan2(dRow, dCol)))
          .distance((int) Math.hypot(dRow, dCol));
        actorInfo.color(actor.getColor());
        environment.add(actorInfo.build());
      }
    }
    this.nextActions = this.brain.eventResponse(event, that, environment);
  }

  @Override
  public void act() {
    if (this.nextActions == null) {
      return;
    }
    for (final Action a : this.nextActions) {
      if (a == null) {
        continue;
      }
      Class<?> clazz = a.getClass();
      BiConsumer<Shell, Action> impl = null;
      while (clazz != null && (impl = this.actionImpls.get(clazz)) == null) {
        clazz = clazz.getSuperclass();
      }
      if (impl != null) {
        impl.accept(this, a);
      }
      if (a.isFinal()) {
        break;
      }
    }
    this.nextActions = null;
  }

  public Map<String, Object> getTags() {
    return this.tags;
  }

  public Watchman getWatchman() {
    return this.watchman;
  }
}
