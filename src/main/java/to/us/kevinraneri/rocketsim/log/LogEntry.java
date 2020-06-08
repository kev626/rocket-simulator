package to.us.kevinraneri.rocketsim.log;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LogEntry {

    private double time;

    private double altitude;
    private double rotationX = Math.toRadians(2);

    private double mass;
    private double velocity;
    private double acceleration;

    private double xPos;
    private double xVel;
    private double xAccel;

    private double angVelocity;
    private double angAcceleration;

    private double tvcX = Math.toRadians(0);
    private double tvcSetX;

}
