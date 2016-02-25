package info.gridworld.actor;

import lombok.Data;
import lombok.EqualsAndHashCode;

public class ActorEvents {
  @Data
  @EqualsAndHashCode(callSuper = true)
  public static class StepEvent extends ActorEvent {
    private static final long serialVersionUID = 1L;

    public StepEvent(final Object source) {
      super(source, "Step");
    }
  }
  @Data
  @EqualsAndHashCode(callSuper = true)
  public static class CollisionEvent extends ActorEvent {
    private static final long serialVersionUID = 1L;
    private final ActorInfo collidedWith;

    public CollisionEvent(final Object source, final ActorInfo collidedWith) {
      super(source, "Collision");
      this.collidedWith = collidedWith;
    }
  }
}
