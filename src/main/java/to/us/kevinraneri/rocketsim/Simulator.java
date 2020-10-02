package to.us.kevinraneri.rocketsim;

import org.spongepowered.noise.Noise;
import org.spongepowered.noise.NoiseQuality;
import to.us.kevinraneri.rocketsim.control.CompositeController;
import to.us.kevinraneri.rocketsim.control.Controller;
import to.us.kevinraneri.rocketsim.control.AngularController;
import to.us.kevinraneri.rocketsim.control.PositionController;
import to.us.kevinraneri.rocketsim.log.LogEntry;
import to.us.kevinraneri.rocketsim.log.StateLog;
import to.us.kevinraneri.rocketsim.settings.RocketProperties;
import to.us.kevinraneri.rocketsim.settings.SimulatorProperties;
import to.us.kevinraneri.rocketsim.util.MathUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Simulator {

    private StateLog log;
    private Controller controller;

    public static void main(String[] args) {

        Simulator simulator = new Simulator();
        simulator.run();

    }

    public void run() {
        log = new StateLog();

        int cyclesUntilBurnComplete = (int) (SimulatorProperties.CONTROL_REFRESH_FREQUENCY * RocketProperties.BURN_TIME);
        double deltaTimeSeconds = 1 / SimulatorProperties.CONTROL_REFRESH_FREQUENCY;

        controller = new CompositeController(/*new PositionController(), */new AngularController());

        boolean isBurning = true;
        double apogee = 0;
        double apogeeTime = 0;
        double topSpeed = 0;
        double topSpeedTime = 0;
        double maxAccel = SimulatorProperties.GRAVITY;
        double maxAccelTime = 0;

        boolean ped = false;
        boolean fallBeforeBurn = false;
        for (int i = 0; log.getLastState().getAltitude() >= 0; i++) {

            isBurning = i <= cyclesUntilBurnComplete;

            double currentTime = i * deltaTimeSeconds;

            if (!isBurning) break;

            runCycle(currentTime, deltaTimeSeconds, isBurning);
            double nowAltitude = log.getLastState().getAltitude();
            double nowVel = log.getLastState().getVelocity();
            double nowAccel = log.getLastState().getAcceleration();

            if (Math.abs(log.getLastState().getRotationX()) > Math.PI / 2) {
                ped = true;
            }

            if (isBurning && nowVel < 0) {
                fallBeforeBurn = true;
            }

            if (nowAltitude > apogee) {
                apogee = nowAltitude;
                apogeeTime = currentTime;
            }

            if (nowVel > topSpeed) {
                topSpeed = nowVel;
                topSpeedTime = currentTime;
            }

            if (nowAccel > maxAccel) {
                maxAccel = nowAccel;
                maxAccelTime = currentTime;
            }
        }

        if (isBurning) {
            System.out.println("[CRASH WARNING]");
            System.out.println("[CRASH WARNING] Rocket hit the ground while still burning propellant");
            System.out.println("[CRASH WARNING]");
        }

        if (fallBeforeBurn) {
            System.out.println("[WARNING] Rocket begins falling before burn complete!");
        }

        if (ped) {
            System.out.println("[WARNING] POINTY END DOWN");
        }

        double durationSeconds = log.getEntryCount() / SimulatorProperties.CONTROL_REFRESH_FREQUENCY;
        double accelG = (maxAccel - SimulatorProperties.GRAVITY) / (-SimulatorProperties.GRAVITY);

        System.out.println("---- [Statistics] ----");
        System.out.println(String.format("Flight duration: %.2f sec", durationSeconds));
        System.out.println(String.format("Apogee: %.2fm @ %.2f sec", apogee, apogeeTime));
        System.out.println(String.format("Max velocity: %.2fm/s @ %.2f sec", topSpeed, topSpeedTime));
        System.out.println(String.format("Max accel: %.2fm/s^2 @ %.2f sec (%.2f g)", maxAccel, maxAccelTime, accelG));

        // dump a CSV
        String csvFile = log.dumpCSV();

        try {
            FileWriter writer = new FileWriter(new File("output.csv"), false);
            writer.write(csvFile);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public void runCycle(double currentTime, double delta, boolean isBurning) {
        LogEntry lastState = log.getLastState();
        double mass = RocketProperties.MASS - MathUtil.lerp(0, RocketProperties.PROPELLANT_MASS, currentTime / RocketProperties.BURN_TIME);

        double tvcAngleFromVertical = lastState.getTvcX() + RocketProperties.TVC_ERROR_X;
        double torque = RocketProperties.MOMENT_ARM * Math.sin(tvcAngleFromVertical) * RocketProperties.THRUST;
        double angAccel = isBurning ? torque / RocketProperties.MASS_MOMENT_OF_INERTIA : 0;
        double angVelocity = lastState.getAngVelocity() + angAccel * delta;

        double newAngle = lastState.getRotationX() + lastState.getAngVelocity() * delta + 0.5 * angAccel * delta * delta;

        if (isBurning) {
            // Simulate wind
            double windAmountRaw = 2 * (Noise.gradientCoherentNoise3D(lastState.getXPos() * .1, lastState.getAltitude() * .1, lastState.getXPos() * .1, 0, NoiseQuality.STANDARD) - 0.5);
            newAngle += windAmountRaw * 0.01;
        }

        double gravitationalForce = mass * SimulatorProperties.GRAVITY;

        double verticalForce = isBurning ? RocketProperties.THRUST * Math.cos(newAngle) * Math.cos(tvcAngleFromVertical) + gravitationalForce : gravitationalForce;
        double rocketAccel = verticalForce / mass;

        double xForce = isBurning ? RocketProperties.THRUST * Math.sin(newAngle) * Math.cos(tvcAngleFromVertical) : 0;
        double rocketXAccel = xForce / mass;

        double newVelocity = lastState.getVelocity() + rocketAccel * delta;
        double newAltitude = lastState.getAltitude() + lastState.getVelocity() * delta + 0.5 * rocketAccel * delta * delta;

        double newXVel = lastState.getXVel() + rocketXAccel * delta;
        double newXPos = lastState.getXPos() + lastState.getXVel() * delta + 0.5 * rocketXAccel * delta * delta;

        double newTvcSetX = controller.runControl(lastState, delta, 0);

        if (newTvcSetX > RocketProperties.TVC_MAX) newTvcSetX = RocketProperties.TVC_MAX;
        if (newTvcSetX < -RocketProperties.TVC_MAX) newTvcSetX = -RocketProperties.TVC_MAX;

        double newTvcAngle = lastState.getTvcX();

        // Here we handle the response rate
        double tvcAngleIncrement = Math.toRadians((1/RocketProperties.TVC_SERVO_SPEED)*60*delta/RocketProperties.TVC_SERVO_THROW);
        if (lastState.getTvcX() < newTvcSetX) {
            // We're incrementing
            newTvcAngle += tvcAngleIncrement;
            if (newTvcAngle > newTvcSetX) {
                newTvcAngle = newTvcSetX;
            }
        } else {
            // We're decrementing
            newTvcAngle -= tvcAngleIncrement;
            if (newTvcAngle < newTvcSetX) {
                newTvcAngle = newTvcSetX;
            }
        }

        LogEntry entry = new LogEntry(currentTime, newAltitude, newAngle, mass, newVelocity, rocketAccel, newXPos, newXVel, rocketXAccel, angVelocity, angAccel, newTvcAngle, newTvcSetX);
        System.out.println(String.format("t=%.2f sec,alt=%.2f,vel=%.2f,accel=%.2f,ang=%.2f deg,downrange=%.2f", currentTime, entry.getAltitude(), newVelocity, rocketAccel, Math.toDegrees(newAngle), newXPos));
        log.addLog(entry);

    }

}
