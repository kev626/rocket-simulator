package to.us.kevinraneri.rocketsim.settings;

public class RocketProperties {

    // Rocket mass in kilograms
    public static final double MASS = 0.949;

    // Average motor thrust in newtons
    public static final double THRUST = 15;

    // Motor burn time in seconds
    public static final double BURN_TIME = 8;

    // Motor propellant mass in kg
    public static final double PROPELLANT_MASS = 0.062;

    // Mass Moment of Inertia in kg * m^2
    public static final double MASS_MOMENT_OF_INERTIA = 0.087706;

    // Moment Arm length in m
    public static final double MOMENT_ARM = 0.355;

    // TVC mount X and Y mounting/trim error in degrees
    public static final double TVC_ERROR_X = Math.toRadians(0);
    public static final double TVC_ERROR_Y = 0;

    // TVC Servo correction speed in sec/60deg
    public static final double TVC_SERVO_SPEED = 0.14;

    // How many servo degrees equates to one TVC mount degree
    public static final double TVC_SERVO_THROW = 10;

    public static final double TVC_MAX = 5; // Maximum TVC throw in any direction.

    public static final double PRESSURE_CONSTANT = 1; // This simulates some aerodynamic drag to make the rocket more unstable.

}
