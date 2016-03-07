package info.gridworld.cashgrab;

import java.awt.Color;
import java.io.Serializable;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import info.gridworld.actor.Action;
import info.gridworld.actor.Actions.ColorAction;
import info.gridworld.actor.Actions.MessageAction;
import info.gridworld.actor.Actions.MoveAction;
import info.gridworld.actor.Actions.TurnAction;
import info.gridworld.actor.ActorEvent;
import info.gridworld.actor.ActorEvent.ActorInfo;
import info.gridworld.actor.ActorEvents.MessageEvent;
import info.gridworld.actor.ActorEvents.StepEvent;
import info.gridworld.actor.ActorListener;
import info.gridworld.actor.Util;
import info.gridworld.actor.Util.Either;
import info.gridworld.cashgrab.Actions.ConsumeAction;
import javafx.util.Pair;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.val;

@RequiredArgsConstructor
public class HunterCritter implements ActorListener {
  private static final int BABY_TIME = 30;

  @Value
  public static class MatingCall implements Serializable {
    @Value
    public static class Info implements Serializable {
      private static final long serialVersionUID = 1L;
      UUID uuid;
      Optional<Integer> id;
      Optional<Double> mateDirection;
      Optional<Double> mateDistance;
      Optional<Double> mateId;
    }

    private static final long serialVersionUID = 1L;
    Info info;
    boolean isFemale;
    int pass;
  }

  private final boolean isFemale;
  private boolean firstRun = true;
  private int time = 0;
  private int lastBaby = 0;
  private Optional<MatingCall.Info> mate = Optional.empty();
  private final UUID uuid = UUID.randomUUID();

  @Override
  public Stream<Action> eventResponse(final ActorEvent e, final ActorInfo self,
    final Set<ActorInfo> environment) {
    val actions = Stream.<Action>builder();
    if (e instanceof MessageEvent) {
      val message_ = ((MessageEvent) e).getMessage();
      matingCall: if (message_ instanceof MatingCall) {
        val message = (MatingCall) message_;
        if (time >= BABY_TIME) {
          if (mate.isPresent()) {
            val mate_ = mate.get();
            if (!mate_.getUuid().equals(message.getInfo().getUuid())) {
              break matingCall;
            }
          }
        }
      }
    } else if (e instanceof StepEvent) {
      finalAction: {
        if (firstRun) {
          actions.add(new ColorAction(Color.ORANGE));
        }
        time++;
        lastBaby++;
        if (lastBaby > BABY_TIME) {
          if (mate.isPresent()) {
            break finalAction;
          }
          val courtAction = court(environment);
          if (courtAction.isPresent()) {
            //mate = Optional.of(courtAction.get());
            actions.add(courtAction.get().getKey());
          }
        }
        val eatAction = eatPrey(environment);
        if (eatAction.isPresent()) {
          actions.add(eatAction.get());
          break finalAction;
        }
        if (Math.random() < 0.5) {
          actions.add(new TurnAction(-1));
        } else {
          actions.add(new TurnAction(1));
        }
        actions.add(new MoveAction(1));
        break finalAction;
      }
    }
    return actions.build();
  }

  private Optional<Pair<Action, ActorInfo>> court(Set<ActorInfo> environment) {
    val name = this.getClass().getName();
    val lover = environment.stream()
      .filter(a -> a.getType().map(t -> t.equals(name)).orElse(true)).findAny();
    return Util.Pairs
      .ofOptional(
        lover
          .flatMap(
            a -> Util.Pairs.ofOptional(a.getDistance(), a.getDirection()))
          .map(
            p -> new MessageAction(Either.right(Either.right(p)), "hey baby")),
      lover);
  }

  private Optional<Action> eatPrey(Set<ActorInfo> environment) {
    val name = this.getClass().getName();
    return environment.stream()
      .filter(a -> !a.getType().map(t -> t.equals(name)).orElse(false))
      .sorted(
        (a1, a2) -> Double.compare(a1.getDistance().orElse(Double.MAX_VALUE),
          a2.getDistance().orElse(Double.MAX_VALUE)))
      .findFirst()
      .flatMap(a -> Util.Pairs.ofOptional(a.getDistance(), a.getDirection()))
      .map(p -> Util.Pairs.apply(p, ConsumeAction::new));
  }
}
