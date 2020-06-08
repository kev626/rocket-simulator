package to.us.kevinraneri.rocketsim.control;

import to.us.kevinraneri.rocketsim.log.LogEntry;
import to.us.kevinraneri.rocketsim.settings.RocketProperties;
import to.us.kevinraneri.rocketsim.util.MathUtil;

public class SmarterController implements Controller {

    private double targetAngle = 0;

    private double elaspsedTime = 0;

    private double targetingTime = 0.5;

    public SmarterController() {

    }

    public double runControl(LogEntry entry, double delta) {
        elaspsedTime += delta;
        // Simulate propellant mass loss
        double mass = RocketProperties.MASS - MathUtil.lerp(0, RocketProperties.PROPELLANT_MASS, elaspsedTime / RocketProperties.BURN_TIME);
        double thrust = Math.sqrt(entry.getAcceleration() * entry.getAcceleration() + entry.getXAccel() * entry.getXAccel()) * mass;

        double targetAccel = (targetAngle - entry.getRotationX() - entry.getAngVelocity() * targetingTime)/(0.5 * targetingTime * targetingTime);
        double targetTorque = targetAccel * RocketProperties.MASS_MOMENT_OF_INERTIA;

        double tvcAngle = Math.asin(targetTorque / (RocketProperties.MOMENT_ARM * thrust));

        return tvcAngle;
    }

}
