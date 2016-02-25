package info.gridworld.cashgrab;

import java.util.function.BiConsumer;

import info.gridworld.actor.Action;
import info.gridworld.actor.Actor;
import info.gridworld.actor.Shell;
import info.gridworld.cashgrab.CashGrab.Bank;
import info.gridworld.grid.Grid;
import info.gridworld.grid.Location;
import lombok.Data;

public class Actions {
  @Data
  public static class CollectCoinAction implements Action {
    private final double direction;
    private final double distance;

    @Override
    public String getType() {
      return "CollectCoin";
    }

    @Override
    public boolean isFinal() {
      return true;
    }

    public static BiConsumer<Shell, Action> impl(final int maxDist,
      final int maxMine) {
      return (final Shell that, final Action a) -> {
        final Bank bank =
          (Bank) that.getTags().get(CashGrab.Tags.MINABLE.getTag());
        if (bank == null) {
          return;
        }
        final double distance =
          Math.min(((CollectCoinAction) a).getDistance(), maxDist);
        final double direction = that.getDirection();
        final Location loc = that.getLocation();
        final Grid<Actor> grid = that.getGrid();
        final int offsetX =
          (int) (distance * Math.cos(Math.toRadians(direction)));
        final int offsetY =
          (int) (distance * Math.sin(Math.toRadians(direction)));
        final Location targetLoc =
          new Location(loc.getRow() + offsetX, loc.getCol() + offsetY);
        final Actor target = grid.get(targetLoc);
        final int id = that.getId();
        int targetId = -1;
        if (target instanceof Shell) {
          targetId = ((Shell) target).getId();
        } else if (target instanceof Coin) {
          targetId = ((Coin) target).getId();
        }
        if (id == -1) {
          return;
        }
        if (bank.getBalance(targetId) > 0) {
          bank.transfer(targetId, id, maxMine);
        }
      };
    }
  }
}
