package info.gridworld.actor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import info.gridworld.actor.Actions.ColorAction;
import info.gridworld.actor.Actions.MoveAction;
import info.gridworld.actor.Actions.TurnAction;
import info.gridworld.actor.ActorEvent.ActorInfo;
import info.gridworld.actor.ShellWorld.Watchman;
import info.gridworld.grid.Location;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

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

  private Stream<Actor> actorsInRadius(int sightRadius) {
    val stream = Stream.<Actor>builder();
    val grid = this.getGrid();
    val maxRow = grid.getNumRows() - 1;
    val maxCol = grid.getNumCols() - 1;
    val myLoc = this.getLocation();
    val myRow = myLoc.getRow();
    val myCol = myLoc.getCol();
    val startRow = Math.max(myRow - sightRadius, 0);
    val endRow = Math.min(myRow + sightRadius,
      (maxRow == -1) ? Integer.MAX_VALUE : maxRow);
    // wheeee, square radii
    for (int row = startRow; row < endRow; row++) {
      val startCol = Math.max(myCol - sightRadius, 0);
      val endCol = Math.min(myCol + sightRadius,
        (maxCol == -1) ? Integer.MAX_VALUE : maxCol);
      for (int col = Math.max(startCol, 0); col < endCol; col++) {
        val loc = new Location(row, col);
        val actor = grid.get(loc);
        if (actor == null) {
          continue;
        }
        stream.add(actor);
      }
    }
    return stream.build();
  }

  public void respond(final ActorEvent event) {
    val that = ActorInfo.builder().id(this.id).distance(0.0)
      .color(this.getColor()).build();
    final Set<ActorInfo> environment = new HashSet<>();
    val myLoc = this.getLocation();
    val myRow = myLoc.getRow();
    val myCol = myLoc.getCol();
    final int sightRadius = 3;
    this.actorsInRadius(sightRadius).forEach(actor -> {
      val actorInfo = ActorInfo.builder().type(actor.getClass().getName());
      val actorLoc = actor.getLocation();
      final int actorRow = actorLoc.getRow();
      final int actorCol = actorLoc.getCol();
      val dRow = actorRow - myRow;
      val dCol = actorCol - myCol;
      actorInfo.direction(Math.toDegrees(Math.atan2(dRow, dCol)))
        .distance(Math.hypot(dRow, dCol));
      actorInfo.color(actor.getColor());
      environment.add(actorInfo.build());
    });
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
