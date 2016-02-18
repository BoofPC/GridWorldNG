package info.gridworld.cashgrab;

import java.util.ArrayList;
import java.util.List;

import info.gridworld.actor.Action;
import info.gridworld.actor.Actions;
import info.gridworld.actor.ActorEvent;
import info.gridworld.actor.ActorEvent.ActorInfo;
import info.gridworld.actor.ActorEvents.TurnEvent;
import info.gridworld.actor.ActorListener;

public class CalebBug implements ActorListener {
  @Override
  public Iterable<Action> eventResponse(ActorEvent e, ActorInfo self,
    List<ActorInfo> environment) {
    final List<Action> actions = new ArrayList<>();
    if (e instanceof TurnEvent) {
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
