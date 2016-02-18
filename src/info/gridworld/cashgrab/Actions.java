package info.gridworld.cashgrab;

import info.gridworld.actor.Action;

public class Actions {
  public static class CollectCoinAction implements Action {
    @Override
    public String getType() {
      return "CollectCoin";
    }

    @Override
    public boolean isFinal() {
      return true;
    }
  }
}
