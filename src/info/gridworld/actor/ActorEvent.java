package info.gridworld.actor;

import java.awt.Color;
import java.util.EventObject;
import java.util.Optional;

public class ActorEvent extends EventObject {
  public static class ActorInfo {
    public final Optional<Integer> id;
    public final Optional<Integer> distance;
    public final Optional<Integer> direction;
    public final Optional<Color> color;

    public ActorInfo(Integer id, Integer distance, Integer direction,
      Color color) {
      this.id = Optional.ofNullable(id);
      this.distance = Optional.ofNullable(distance);
      this.direction = Optional.ofNullable(direction);
      this.color = Optional.ofNullable(color);
    }

    public static Builder builder() {
      return new Builder();
    }

    public static class Builder {
      private Integer id;
      private Integer distance;
      private Integer direction;
      private Color color;

      private Builder() {};

      public Builder id(Integer id) {
        this.id = id;
        return this;
      }

      public Builder distance(Integer distance) {
        this.distance = distance;
        return this;
      }

      public Builder direction(Integer direction) {
        this.direction = direction;
        return this;
      }

      public Builder color(Color color) {
        this.color = color;
        return this;
      }

      public ActorInfo build() {
        return new ActorInfo(id, distance, direction, color);
      }
    }
  }

  private static final long serialVersionUID = 1L;
  private final String type;

  public ActorEvent(Object source, String type) {
    super(source);
    this.type = type;
  }

  public String getType() {
    return type;
  }
}