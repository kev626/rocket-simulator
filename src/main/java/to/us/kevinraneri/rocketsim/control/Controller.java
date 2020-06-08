package to.us.kevinraneri.rocketsim.control;

import to.us.kevinraneri.rocketsim.log.LogEntry;

public interface Controller {

    double runControl(LogEntry entry, double delta);

}
