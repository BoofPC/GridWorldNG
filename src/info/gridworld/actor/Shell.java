package info.gridworld.actor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import info.gridworld.actor.Actions.ColorAction;
import info.gridworld.actor.Actions.MoveAction;
import info.gridworld.actor.Actions.TurnAction;
import info.gridworld.actor.ActorEvent.ActorInfo;
import info.gridworld.actor.ShellWorld.Watchman;

public class Shell extends Actor {
  public enum Tags {
    PUSHABLE("Shell.Pushable");
    private final String str;

    private Tags(final String str) {
      this.str = str;
    }

    public String get() {
      return str;
    }
  }

  private final int id;
  private final ActorListener brain;
  private final Watchman watchman;
  private Iterable<Action> nextActions;
  private final Map<Class<? extends Action>, BiConsumer<Shell, Action>> actionImpls =
    new HashMap<>();
  private final Set<String> tags = new HashSet<>();

  public Shell(int id, ActorListener brain, Watchman watchman) {
    this.id = id;
    this.brain = brain;
    this.watchman = watchman;
    actionImpls.put(MoveAction.class, MoveAction.impl(1));
    actionImpls.put(TurnAction.class, TurnAction.impl());
    actionImpls.put(ColorAction.class, ColorAction.impl());
  }

  public void respond(ActorEvent event) {
    final ActorInfo self =
      ActorInfo.builder().id(id).distance(0).color(this.getColor()).build();
    final List<ActorInfo> environment = new ArrayList<>();
    nextActions = this.brain.eventResponse(event, self, environment);
  }

  @Override
  public void act() {
    if (this.nextActions == null) {
      return;
    }
    for (final Action a : nextActions) {
      final Class<? extends Action> clazz = a.getClass();
      final BiConsumer<Shell, Action> impl = this.actionImpls.get(clazz);
      if (impl != null) {
        impl.accept(this, a);
      }
      if (a.isFinal()) {
        break;
      }
    }
    this.nextActions = null;
  }

  public Set<String> getTags() {
    return tags;
  }

  public Watchman getWatchman() {
    return watchman;
  }
}
