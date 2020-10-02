package to.us.kevinraneri.rocketsim.control;

import org.apache.commons.math3.complex.Complex;
import to.us.kevinraneri.rocketsim.log.LogEntry;
import to.us.kevinraneri.rocketsim.settings.RocketProperties;
import to.us.kevinraneri.rocketsim.util.MathUtil;

public class AngularController implements Controller {

    private double elaspsedTime = 0;

    // Desired eigenvalues
    private Complex a = new Complex(-2, 2);
    private Complex b = new Complex(-2, -2);

    //k1 = -abI
    //k2 = I(a+b)

    private double integral = 0;
    private double kI = 0.26;
    public AngularController() {

    }

    public double runControl(LogEntry entry, double delta, double setpoint) {

        double k1 = -RocketProperties.MASS_MOMENT_OF_INERTIA * a.multiply(b).getReal();
        double k2 = RocketProperties.MASS_MOMENT_OF_INERTIA * a.add(b).getReal();

        elaspsedTime += delta;
        // Simulate propellant mass loss

        double error = setpoint - entry.getRotationX();
        integral += error * delta;

        double mass = RocketProperties.MASS - MathUtil.lerp(0, RocketProperties.PROPELLANT_MASS, elaspsedTime / RocketProperties.BURN_TIME);
        double thrust = Math.sqrt(entry.getAcceleration() * entry.getAcceleration() + entry.getXAccel() * entry.getXAccel()) * mass;

        double targetTorque = k1*entry.getRotationX() + k2*entry.getAngVelocity();

        double tvcAngle = Math.asin(targetTorque / (RocketProperties.MOMENT_ARM * thrust));

        return tvcAngle + kI * integral;
    }

}
