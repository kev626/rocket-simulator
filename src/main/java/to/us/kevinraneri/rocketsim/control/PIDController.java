package to.us.kevinraneri.rocketsim.control;

import to.us.kevinraneri.rocketsim.log.LogEntry;

public class PIDController implements Controller {

    private final double kP = 0.134;
    private final double kI = 0.144;
    private final double kD = 0.074;

    private double integral = 0;
    private double prevError = 0;

    private double targetAngle = 0;

    public PIDController() {

    }

    public double runControl(LogEntry entry, double delta) {
        double error = targetAngle - entry.getRotationX();

        integral += delta * error;

        double derivative = (error - prevError)/delta;
        prevError = error;

        return kP * error + kI * integral + kD * derivative;
    }

}
