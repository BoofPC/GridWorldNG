package info.gridworld.actor;

import java.util.EventListener;
import java.util.List;

import info.gridworld.actor.ActorEvent.ActorInfo;

public interface ActorListener extends EventListener {
  Iterable<Action> eventResponse(ActorEvent e, ActorInfo self,
    List<ActorInfo> environment);
}
