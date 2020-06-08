package to.us.kevinraneri.rocketsim.control;

import to.us.kevinraneri.rocketsim.log.LogEntry;
import to.us.kevinraneri.rocketsim.settings.RocketProperties;
import to.us.kevinraneri.rocketsim.util.MathUtil;

/**
 * This controller generates a target rocket angle to null out any horizontal drift
 */
public class PositionController implements Controller {

    private double elapsedTime = 0;

    private double targetingTime = 4;

    private double integratingXVel = 0;
    private double integratingXPos = 0;

    public PositionController() {

    }

    public double runControl(LogEntry entry, double delta, double setpoint) {
        elapsedTime += delta;

        // Work out the x position and velocity
        double xAccel = entry.getXAccel();
        integratingXPos += integratingXVel * delta + 0.5 * xAccel * delta * delta;
        integratingXVel += xAccel * delta;

        // Work out the target X acceleration
        double targetXAccel = (setpoint - integratingXPos - integratingXVel * targetingTime)/(0.5 * targetingTime * targetingTime);

        // Determine thrust from acceleration
        double mass = RocketProperties.MASS - MathUtil.lerp(0, RocketProperties.PROPELLANT_MASS, elapsedTime / RocketProperties.BURN_TIME);
        double thrust = Math.sqrt(entry.getAcceleration() * entry.getAcceleration() + entry.getXAccel() * entry.getXAccel()) * mass;

        double targetXRot = Math.asin(targetXAccel / thrust);

        System.out.println(String.format("t=%.2f,txa=%.2f,txr=%.2f deg", thrust, targetXAccel, Math.toDegrees(targetXRot)));

        return targetXRot;
    }

}
