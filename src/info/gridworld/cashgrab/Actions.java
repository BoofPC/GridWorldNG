package info.gridworld.cashgrab;

import java.util.function.BiConsumer;

import info.gridworld.actor.Action;
import info.gridworld.actor.Actor;
import info.gridworld.actor.Shell;
import info.gridworld.actor.Util;
import info.gridworld.actor.Util.Pairs;
import info.gridworld.cashgrab.CashGrab.Bank;
import info.gridworld.grid.Location;
import javafx.util.Pair;
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
        @SuppressWarnings("unchecked") val bank_ =
          (Pair<Bank, Integer>) that.getTag(CashGrab.Tags.BANK);
        if (bank_ == null || bank_.getKey() == null
          || bank_.getValue() == null) {
          System.out.println("null bank of " + that.getId());
          return;
        }
        val cca = (CollectCoinAction) a;
        final double distance = Math.min(cca.getDistance(), maxDist);
        final double direction = cca.getDirection();
        System.out.println(that.getId() + " at " + that.getLocation()
          + " attempting collection of distance " + distance + " direction "
          + that.getDirection() + " target direction " + direction);
        val loc = Util.locToRect(that.getLocation());
        val grid = that.getGrid();
        val offset = Util.polarToRect(distance,
          Util.polarRight(Math.toRadians(that.getDirection() + direction)));
        val targetLoc =
          Util.rectToLoc(Pairs.thread(loc, offset, (x, y) -> x + y));
        System.out.println("targeting " + targetLoc + " offset " + offset);
        final Actor target_ = grid.get(Util.sanitize(targetLoc, grid));
        if (target_ == null) {
          System.out.println("null target");
          return;
        }
        Pair<Bank, Integer> targetBank_ = null;
        if (target_ instanceof Shell) {
          val target = (Shell) target_;
          if (!(boolean) target.getTagOrDefault(CashGrab.Tags.MINABLE, false)) {
            System.out.println("non-minable target");
            return;
          }
          @SuppressWarnings({"unchecked"}) val targetBank__ =
            ((Pair<Bank, Integer>) target.getTagOrDefault(CashGrab.Tags.BANK,
              null));
          targetBank_ = targetBank__;
        } else if (target_ instanceof Coin) {
          val target = (Coin) target_;
          targetBank_ = Pairs.liftNull(target.getBank(), target.getId());
        } else {
          System.out.println("unknown target");
          return;
        }
        if (targetBank_ == null) {
          System.out.println("null target bank");
          return;
        }
        final Bank bank = bank_.getKey();
        final int id = bank_.getValue();
        final Bank targetBank = targetBank_.getKey();
        final int targetId = targetBank_.getValue();
        System.out.println("transfer attempt " + id + " to " + targetId);
        if (targetBank.getBalance(targetId) > 0) {
          bank.transfer(targetBank, targetId, id, maxMine);
          System.out
            .println("transfer " + id + " to " + targetId + " complete");
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
          System.out.println("not a pred");
          return;
        }
        val loc = that.getLocation();
        val ca = ((ConsumeAction) a);
        val grid = that.getGrid();
        val offsets = Util.polarToRect(ca.getDistance(), Util
          .polarRight(Math.toRadians(that.getDirection() + ca.getDirection())));
        val preyLoc = new Location((int) (loc.getRow() + offsets.getKey()),
          (int) (loc.getCol() + offsets.getValue()));
        val prey = grid.get(Util.sanitize(preyLoc, grid));
        if (prey == null) {
          System.out.println("nothing to eat");
          return;
        }
        if (prey == that) {
          System.out.println("can't eat self");
          return;
        }
        System.out.println("om nom nom");
        prey.removeSelfFromGrid();
      };
    }
  }
}
