package info.gridworld.cashgrab;

import java.util.HashMap;
import java.util.Map;

import info.gridworld.actor.Shell;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

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
      final int srcBalance = this.balances.getOrDefault(src, 0);
      final int destBalance = this.balances.getOrDefault(dest, 0);
      this.balances.put(src, srcBalance - amount);
      this.balances.put(dest, destBalance + amount);
      return this;
    }
  }
}
