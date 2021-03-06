package info.gridworld.cashgrab;

import java.awt.Color;
import java.io.Serializable;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

import info.gridworld.actor.Action;
import info.gridworld.actor.Actions.ColorAction;
import info.gridworld.actor.Actions.MessageAction;
import info.gridworld.actor.ActorEvent;
import info.gridworld.actor.ActorEvent.ActorInfo;
import info.gridworld.actor.ActorEvents.MessageEvent;
import info.gridworld.actor.ActorEvents.StepEvent;
import info.gridworld.actor.ActorListener;
import info.gridworld.actor.Util;
import info.gridworld.actor.Util.Either;
import info.gridworld.actor.Util.Pairs;
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
      Integer id;
      Double mateDirection;
      Double mateDistance;
      Double mateId;
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
  private MatingCall.Info mate = null;
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
          if (mate != null) {
            if (!mate.getUuid().equals(message.getInfo().getUuid())) {
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
          if (mate != null) {
            break finalAction;
          }
          val courtAction = court(environment);
          if (courtAction != null) {
            //mate = Optional.of(courtAction.get());
            actions.add(courtAction.getKey());
          }
        }
        val eatAction = eatPrey(environment);
        if (eatAction != null) {
          actions.add(eatAction);
          break finalAction;
        }
        /*if (Math.random() < 0.5) {
          actions.add(new TurnAction(-1));
        } else {
          actions.add(new TurnAction(1));
        }
        actions.add(new MoveAction(1));*/
        break finalAction;
      }
    }
    return actions.build();
  }

  private Pair<Action, ActorInfo> court(Set<ActorInfo> environment) {
    val name = this.getClass().getName();
    val lover = environment.stream()
      .filter(a -> Util.coalesce(a.getType(), "").equals(name)).findAny()
      .orElse(null);
    if (lover == null) {
      return null;
    }
    val loverLocation =
      Pairs.liftNull(lover.getDistance(), lover.getDirection());
    final Function<Pair<Double, Double>, MessageAction> messageFun =
      p -> new MessageAction(Either.right(Either.right(p)), "hey baby");
    return Pairs.liftNull(Util.applyNullable(loverLocation, messageFun), lover);
  }

  private Action eatPrey(Set<ActorInfo> environment) {
    val name = this.getClass().getName();
    val prey = environment.stream()
      .filter(a -> !Util.coalesce(a.getType(), "").equals(name))
      .sorted((a1, a2) -> Double.compare(
        Util.coalesce(a1.getDistance(), Double.MAX_VALUE),
        Util.coalesce(a2.getDistance(), Double.MAX_VALUE)))
      .findFirst().orElse(null);
    if (prey == null) {
      return null;
    }
    return Pairs.applyNullable(
      Pairs.liftNull(prey.getDistance(), prey.getDirection()),
      ConsumeAction::new);
  }
}
