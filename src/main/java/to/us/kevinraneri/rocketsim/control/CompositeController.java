package to.us.kevinraneri.rocketsim.control;

import to.us.kevinraneri.rocketsim.log.LogEntry;

/**
 * A controller that composes other controllers together.
 *
 * The output from the previous controller is used as the input for the next.
 */
public class CompositeController implements Controller {

    private Controller[] controllers;

    public CompositeController(Controller... controllers) {
        this.controllers = controllers;
    }

    @Override
    public double runControl(LogEntry entry, double delta, double setpoint) {
        double lastOutput = setpoint;
        for (Controller controller : controllers) {
            lastOutput = controller.runControl(entry, delta, lastOutput);
        }
        return lastOutput;
    }
}
