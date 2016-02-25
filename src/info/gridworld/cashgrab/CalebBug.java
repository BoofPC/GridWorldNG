package info.gridworld.cashgrab;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import info.gridworld.actor.Action;
import info.gridworld.actor.Actions;
import info.gridworld.actor.ActorEvent;
import info.gridworld.actor.ActorEvent.ActorInfo;
import info.gridworld.actor.ActorEvents.StepEvent;
import info.gridworld.actor.ActorListener;

public class CalebBug implements ActorListener {
  @Override
  public Iterable<Action> eventResponse(final ActorEvent e,
    final ActorInfo self, final Set<ActorInfo> environment) {
    final List<Action> actions = new ArrayList<>();
    if (e instanceof StepEvent) {
      if (Math.random() < 0.5) {
        actions.add(new Actions.TurnAction(-2));
      } else {
        actions.add(new Actions.TurnAction(2));
      }
      actions.add(new Actions.MoveAction(1));
    }
    return actions;
  }
}
