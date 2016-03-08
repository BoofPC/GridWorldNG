import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import info.gridworld.actor.Actor;
import info.gridworld.actor.ActorListener;
import info.gridworld.actor.Shell;
import info.gridworld.actor.ShellWorld;
import info.gridworld.cashgrab.CalebBug;
import info.gridworld.cashgrab.CashGrab;
import info.gridworld.grid.BoundedGrid;
import info.gridworld.grid.Grid;

public class CashGrabRunner {
  public static void main(String[] args) {
    final Grid<Actor> grid = new BoundedGrid<>(100, 100);
    final ShellWorld world = new ShellWorld(grid);
    final Stream.Builder<Actor> players = Stream.builder();
    final AtomicReference<Integer> id = new AtomicReference<>(0);
    final Stream.Builder<ActorListener> brains = Stream.builder();
    CashGrab.addShells(players, world, id, Stream.concat(brains.build(),
      Stream.generate(CalebBug::new).limit(1000)));
    CashGrab.scatter(world,
      players.build().filter(a -> a instanceof Shell).map(a -> (Shell) a)
        .filter(s -> s.getBrain() instanceof CalebBug)
        .map(s -> s.tag(Shell.Tags.PUSHABLE)));
    final AtomicReference<Boolean> isFemale = new AtomicReference<>(true);
    /*
    CashGrab.scatter(world, Stream.<Actor>generate(() -> {
      final boolean female = isFemale.getAndUpdate(b -> !b);
      return CashGrab.genShell(world, id, new HunterCritter(female))
        .tag(CashGrab.Tags.IS_FEMALE.getTag(), female);
    }).limit(10));
    */
    world.show();
  }
}
