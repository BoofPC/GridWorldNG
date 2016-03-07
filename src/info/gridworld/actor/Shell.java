package info.gridworld.actor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import info.gridworld.actor.Actions.ColorAction;
import info.gridworld.actor.Actions.MoveAction;
import info.gridworld.actor.Actions.TurnAction;
import info.gridworld.actor.ActorEvent.ActorInfo;
import info.gridworld.actor.ShellWorld.Watchman;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

public class Shell extends Actor {
  @Getter
  @RequiredArgsConstructor
  public enum Tags implements info.gridworld.actor.Tag {
    PUSHABLE("Shell.Pushable");
    private final String tag;
  }

  @Getter private final int id;
  @Getter private final @NonNull ActorListener brain;
  @Getter private final @NonNull Watchman watchman;
  private Stream<Action> nextActions;
  private final @NonNull Map<Class<? extends Action>, BiConsumer<Shell, Action>> actionImpls =
    new HashMap<>();
  @Getter private final @NonNull Map<String, Object> tags = new HashMap<>();

  public Shell(final int id, final @NonNull ActorListener brain,
    final @NonNull Watchman watchman) {
    this.id = id;
    this.brain = brain;
    this.watchman = watchman;
    this.actionImpls.put(MoveAction.class, MoveAction.impl(1));
    this.actionImpls.put(TurnAction.class, TurnAction.impl());
    this.actionImpls.put(ColorAction.class, ColorAction.impl());
  }

  public Shell tag(String tag) {
    this.tag(tag, null);
    return this;
  }

  public Shell tag(String tag, Object value) {
    this.tags.put(tag, value);
    return this;
  }

  public Shell tag(Tag tag) {
    return this.tag(tag.getTag());
  }

  public Shell tag(Tag tag, Object value) {
    return this.tag(tag.getTag(), value);
  }

  public Optional<Object> getTag(String tag) {
    return Optional.ofNullable(this.tags.get(tag));
  }

  public Optional<Object> getTag(Tag tag) {
    return Optional.ofNullable(this.tags.get(tag.getTag()));
  }

  public void respond(final ActorEvent event) {
    val that = ActorInfo.builder().id(Optional.of(this.id))
      .distance(Optional.of(0.0)).color(Optional.of(this.getColor())).build();
    final Set<ActorInfo> environment = new HashSet<>();
    val myLoc = this.getLocation();
    val myLocRect = Util.locToRect(myLoc);
    final int sightRadius = 3;
    Util.actorsInRadius(this, sightRadius).forEach(actor -> {
      val actorInfo =
        ActorInfo.builder().type(Optional.of(actor.getClass().getName()));
      val actorLoc = actor.getLocation();
      val actorLocRect = Util.locToRect(actorLoc);
      val offset = Util.Pairs.thread(myLocRect, actorLocRect, (x, y) -> x - y);
      val offsetPolar = Util.Pairs.apply(offset, Util::rectToPolar);
      actorInfo.distance(Optional.of(offsetPolar.getKey()))
        .direction(Optional.of(Util.normalizeDegrees(
          actor.getDirection() + Math.toDegrees(offsetPolar.getValue()))));
      actorInfo.color(Optional.of(actor.getColor()));
      environment.add(actorInfo.build());
    });
    this.nextActions = this.brain.eventResponse(event, that, environment);
  }

  @Override
  public void act() {
    if (this.nextActions == null) {
      return;
    }
    for (final Action a : (Iterable<Action>) this.nextActions::iterator) {
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
}
