package info.gridworld.actor;

public class ActorEvents {
  public static class TurnEvent extends ActorEvent {
    private static final long serialVersionUID = 1L;

    public TurnEvent(final Object source) {
      super(source, "Turn");
    }
  }
  public static class CollisionEvent extends ActorEvent {
    private static final long serialVersionUID = 1L;
    private final ActorInfo collidedWith;

    public CollisionEvent(final Object source, final ActorInfo collidedWith) {
      super(source, "Collision");
      this.collidedWith = collidedWith;
    }

    public ActorInfo getCollidedWith() {
      return this.collidedWith;
    }
  }
}
