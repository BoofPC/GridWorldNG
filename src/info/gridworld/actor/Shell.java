package info.gridworld.actor;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import info.gridworld.actor.Actions.ColorAction;
import info.gridworld.actor.Actions.MoveAction;
import info.gridworld.actor.Actions.TurnAction;
import info.gridworld.actor.ActorEvent.ActorInfo;
import info.gridworld.grid.Location;

public class Shell extends Actor {
  private final int id;
  private final ActorListener brain;
  private Iterable<Action> nextActions;

  public Shell(int id, ActorListener brain) {
    this.id = id;
    this.brain = brain;
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
      if (a instanceof MoveAction) {
        final int distance = ((MoveAction) a).getDistance();
        final int direction = this.getDirection();
        Location destination = this.getLocation();
        for (int i = distance; i > 0; i--) {
          destination.getAdjacentLocation(direction);
        }
        this.moveTo(destination);
      } else if (a instanceof TurnAction) {
        final int angle = ((TurnAction) a).getAngle();
        final int direction = this.getDirection();
        this.setDirection((direction + angle * 45) % 360);
      } else if (a instanceof ColorAction) {
        final Color color = ((ColorAction) a).getColor();
        this.setColor(color);
      }
      if (a.isFinal()) {
        break;
      }
    }
    this.nextActions = null;
  }
}
