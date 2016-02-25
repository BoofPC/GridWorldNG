package info.gridworld.cashgrab;

import info.gridworld.actor.Rock;
import info.gridworld.cashgrab.CashGrab.Bank;
import lombok.Data;

@Data
public class Coin extends Rock {
  private final int id;
  private final Bank bank;
}
