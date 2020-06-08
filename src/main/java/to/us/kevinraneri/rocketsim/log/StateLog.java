package to.us.kevinraneri.rocketsim.log;

import java.util.LinkedList;
import java.util.NoSuchElementException;

public class StateLog {

    private static LinkedList<LogEntry> logs;

    public StateLog() {
        logs = new LinkedList<>();
    }

    public void addLog(LogEntry log) {
        logs.add(log);
    }

    public LogEntry getLastState() {
        try {
            LogEntry log = logs.getLast();
            return log;
        } catch (NoSuchElementException e) {
            return new LogEntry();
        }
    }

    public int getEntryCount() {
        return logs.size();
    }

    public String dumpCSV() {
        String file = "time,altitude,rotx,mass,velocity,acceleration,xpos,xvel,xaccel,angvel,angaccel,tvcx,tvcspx\n";
        for (LogEntry log : logs) {
            file += String.format("%.8f,%.8f,%.8f,%.8f,%.8f,%.8f,%.8f,%.8f,%.8f,%.8f,%.8f,%.8f,%.8f%n",
                    log.getTime(),
                    log.getAltitude(),
                    log.getRotationX(),
                    log.getMass(),
                    log.getVelocity(),
                    log.getAcceleration(),
                    log.getXPos(),
                    log.getXVel(),
                    log.getXAccel(),
                    log.getAngVelocity(),
                    log.getAngAcceleration(),
                    log.getTvcX(),
                    log.getTvcSetX());
        }
        return file;
    }
}
