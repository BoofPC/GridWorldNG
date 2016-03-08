package info.gridworld.cashgrab;

import java.util.function.BiConsumer;

import info.gridworld.actor.Action;
import info.gridworld.actor.Actor;
import info.gridworld.actor.Shell;
import info.gridworld.actor.Util;
import info.gridworld.cashgrab.CashGrab.Bank;
import info.gridworld.grid.Location;
import lombok.Data;
import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Actions {
  @Data
  public class CollectCoinAction implements Action {
    private final double distance;
    private final double direction;

    @Override
    public boolean isFinal() {
      return true;
    }

    public static BiConsumer<Shell, Action> impl(final int maxDist,
      final int maxMine) {
      return (final Shell that, final Action a) -> {
        val bank = (Bank) that.getTags().get(CashGrab.Tags.MINABLE.getTag());
        if (bank == null) {
          return;
        }
        final double distance =
          Math.min(((CollectCoinAction) a).getDistance(), maxDist);
        final double direction = that.getDirection();
        val loc = that.getLocation();
        val grid = that.getGrid();
        val offsets =
          Util.polarToRect(distance, direction + that.getDirection());
        val targetLoc = new Location((int) (loc.getRow() + offsets.getKey()),
          (int) (loc.getCol() + offsets.getValue()));
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
  @Data
  public class ConsumeAction implements Action {
    private final double distance;
    private final double direction;

    @Override
    public boolean isFinal() {
      return true;
    }

    public static BiConsumer<Shell, Action> impl(final int maxDist) {
      return (final Shell that, final Action a) -> {
        if (!((boolean) that.getTagOrDefault(CashGrab.Tags.PREDATOR, false))) {
          return;
        }
        val loc = that.getLocation();
        val ca = ((ConsumeAction) a);
        val grid = that.getGrid();
        val offsets = Util.polarToRect(ca.getDistance(),
          ca.getDirection() + that.getDirection());
        val preyLoc = new Location((int) (loc.getRow() + offsets.getKey()),
          (int) (loc.getCol() + offsets.getValue()));
        val prey = grid.get(preyLoc);
        if (prey != null) {
          prey.removeSelfFromGrid();
        }
      };
    }
  }
}
