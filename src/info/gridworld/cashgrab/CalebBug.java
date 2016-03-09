package info.gridworld.cashgrab;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import info.gridworld.actor.Action;
import info.gridworld.actor.Actions.MoveAction;
import info.gridworld.actor.Actions.TurnAction;
import info.gridworld.actor.ActorEvent;
import info.gridworld.actor.ActorEvent.ActorInfo;
import info.gridworld.actor.ActorEvents.StepEvent;
import info.gridworld.actor.ActorListener;
import info.gridworld.cashgrab.Actions.CollectCoinAction;
import lombok.Getter;
import lombok.ToString;

@ToString
public class CalebBug implements ActorListener {
  @Getter private UUID uuid = UUID.randomUUID();

  @Override
  public Stream<Action> eventResponse(final ActorEvent e, final ActorInfo self,
    final Set<ActorInfo> environment) {
    final List<Action> actions = new ArrayList<>();
    stepEvent: if (e instanceof StepEvent) {
      final ActorInfo coin = environment.stream()
        .filter(a -> a.getType().equals(Coin.class.getName())).findFirst()
        .orElse(null);
      if (coin != null) {
        final Double distance = coin.getDistance();
        final Double direction = coin.getDirection();
        System.out
          .print(uuid + " coin detected " + distance + ", " + direction);
        if (direction != null && distance != null && distance <= 2) {
          System.out.println(", collecting");
          actions.add(new CollectCoinAction(distance, direction));
          break stepEvent;
        }
        System.out.println();
      }
      if (Math.random() < 0.5) {
        actions.add(new TurnAction(-1));
      } else {
        actions.add(new TurnAction(1));
      }
      actions.add(new MoveAction(1));
    }
    return actions.stream();
  }
}
