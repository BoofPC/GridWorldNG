package info.gridworld.cashgrab;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import info.gridworld.actor.Actor;
import info.gridworld.actor.ActorListener;
import info.gridworld.actor.Shell;
import info.gridworld.actor.ShellWorld;
import info.gridworld.world.World;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

public class CashGrab {
  @Getter
  @RequiredArgsConstructor
  public enum Tags implements info.gridworld.actor.Tags {
    MINABLE("CashGrab.Minable", null);
    private final String tag;
    private final Object initial;
  }
  public static class Bank {
    private final Map<Integer, Integer> balances = new HashMap<>();

    public int getBalance(int id) {
      return balances.getOrDefault(id, 0);
    }

    public Bank minable(Shell shell) {
      shell.getTags().put(CashGrab.Tags.MINABLE.getTag(), this);
      return this;
    }

    public Bank transfer(int src, int dest, int amount) {
      val srcBalance = this.balances.getOrDefault(src, 0);
      val destBalance = this.balances.getOrDefault(dest, 0);
      this.balances.put(src, srcBalance - amount);
      this.balances.put(dest, destBalance + amount);
      return this;
    }
  }

  public static Stream.Builder<Actor> addShell(Stream.Builder<Actor> actors,
    ShellWorld world, AtomicReference<Integer> id, ActorListener brain) {
    return actors.add(new Shell(id.getAndUpdate(x -> x + 1), new CalebBug(),
      world.getWatchman()));
  }

  public static Stream.Builder<Actor> addShells(Stream.Builder<Actor> actors,
    ShellWorld world, AtomicReference<Integer> id,
    Stream<ActorListener> brains) {
    brains.forEach(brain -> addShell(actors, world, id, brain));
    return actors;
  }

  public static <T> void scatter(World<T> world, Stream<T> things) {
    things.forEach(t -> Optional.ofNullable(world.getRandomEmptyLocation())
      .ifPresent(loc -> world.add(loc, t)));
  }
}
