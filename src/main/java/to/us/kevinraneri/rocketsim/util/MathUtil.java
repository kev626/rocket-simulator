package to.us.kevinraneri.rocketsim.util;

import org.apache.commons.math3.complex.Quaternion;

public class MathUtil {

    public static double lerp(double a, double b, double f) {
        return a + f * (b - a);
    }

    public static double getAngleFromVert(Quaternion quat) {
        Quaternion q = quat.normalize().getInverse().multiply(Quaternion.J).normalize();
        return 2 * Math.atan2(vLen(q.getVectorPart()), q.getQ0());
    }

    public static double vLen(double[] ds) {
        double sum = 0;
        for (double d : ds) {
            sum += d * d;
        }
        return Math.sqrt(sum);
    }

}
