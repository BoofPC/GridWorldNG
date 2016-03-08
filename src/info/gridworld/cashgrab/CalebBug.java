package info.gridworld.cashgrab;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import info.gridworld.actor.Action;
import info.gridworld.actor.Actions.MoveAction;
import info.gridworld.actor.Actions.TurnAction;
import info.gridworld.actor.ActorEvent;
import info.gridworld.actor.ActorEvent.ActorInfo;
import info.gridworld.actor.ActorEvents.StepEvent;
import info.gridworld.actor.ActorListener;
import info.gridworld.cashgrab.Actions.CollectCoinAction;

public class CalebBug implements ActorListener {
  @Override
  public Stream<Action> eventResponse(final ActorEvent e, final ActorInfo self,
    final Set<ActorInfo> environment) {
    final List<Action> actions = new ArrayList<>();
    if (e instanceof StepEvent) {
      final ActorInfo coin = environment.stream()
        .filter(a -> a.getType().equals(Coin.class.getName())).findFirst()
        .orElse(null);
      if (coin != null) {
        final Double direction = coin.getDirection();
        final Double distance = coin.getDistance();
        if (direction != null && distance != null) {
          actions.add(new CollectCoinAction(direction, distance));
        }
      } else {
        if (Math.random() < 0.5) {
          actions.add(new TurnAction(-1));
        } else {
          actions.add(new TurnAction(1));
        }
        actions.add(new MoveAction(1));
      }
    }
    return actions.stream();
  }
}
