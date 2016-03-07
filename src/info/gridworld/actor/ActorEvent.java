package info.gridworld.actor;

import java.awt.Color;
import java.util.EventObject;
import java.util.Optional;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Value;

@Data
@EqualsAndHashCode(callSuper = true)
public class ActorEvent extends EventObject {
  private static final long serialVersionUID = 1L;

  @Value
  @Builder
  public static class ActorInfo {
    @NonNull Optional<Integer> id;
    @NonNull Optional<String> type;
    @NonNull Optional<Double> distance;
    @NonNull Optional<Double> direction;
    @NonNull Optional<Color> color;
  }

  private final String type;

  public ActorEvent(final Object source, final String type) {
    super(source);
    this.type = type;
  }
}
