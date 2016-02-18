package info.gridworld.actor;

import java.util.EventObject;

public class ReportEvent extends EventObject {
  private static final long serialVersionUID = 1L;

  public ReportEvent(Object source) {
    super(source);
  }
}