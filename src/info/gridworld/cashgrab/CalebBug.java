package info.gridworld.cashgrab;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
  public Iterable<Action> eventResponse(final ActorEvent e,
    final ActorInfo self, final Set<ActorInfo> environment) {
    final List<Action> actions = new ArrayList<>();
    if (e instanceof StepEvent) {
      final Optional<ActorInfo> coin = environment.stream()
        .filter(a -> a.getType().equals("Coin")).findFirst();
      if (coin.isPresent()) {
        actions.add(new CollectCoinAction(coin.get().getDirection(),
          coin.get().getDistance()));
      } else {
        if (Math.random() < 0.5) {
          actions.add(new TurnAction(-2));
        } else {
          actions.add(new TurnAction(2));
        }
        actions.add(new MoveAction(1));
      }
    }
    return actions;
  }
}
