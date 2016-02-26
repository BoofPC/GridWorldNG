import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import info.gridworld.actor.Actor;
import info.gridworld.actor.ActorListener;
import info.gridworld.actor.ShellWorld;
import info.gridworld.cashgrab.CalebBug;
import info.gridworld.cashgrab.CashGrab;
import info.gridworld.grid.BoundedGrid;
import info.gridworld.grid.Grid;

public class CashGrabRunner {
  public static void main(String[] args) {
    final Grid<Actor> grid = new BoundedGrid<>(50, 50);
    final ShellWorld world = new ShellWorld(grid);
    final Stream.Builder<Actor> actors = Stream.builder();
    final AtomicReference<Integer> id = new AtomicReference<>(0);
    final Stream.Builder<ActorListener> brains = Stream.builder();
    brains.add(new CalebBug()).add(new CalebBug()).add(new CalebBug());
    IntStream.range(1, 50).forEach(n -> brains.add(new CalebBug()));
    CashGrab.addShells(actors, world, id, brains.build());
    CashGrab.scatter(world, actors.build());
    world.show();
  }
}
