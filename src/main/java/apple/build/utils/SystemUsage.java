package apple.build.utils;


import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class SystemUsage {
    private static final OperatingSystemMXBean OS;
    private static final long UPDATE_INTERVAL = 2000;

    static {
        java.lang.management.OperatingSystemMXBean s = ManagementFactory.getOperatingSystemMXBean();
        if (s instanceof com.sun.management.OperatingSystemMXBean os) {
            OS = os;
        } else {
            OS = null;
        }
    }

    /**
     * @return a value between 0 and 1 depending on cpu usage
     */
    public static double getProcessCpuLoad() {
        return OS.getProcessCpuLoad();
    }

    /**
     * @return the available processors available to the os
     */
    public static double getProcessorCount() {
        return OS.getAvailableProcessors();
    }

    private static long lastCheckedProcessorsFree = 0;
    private static double processorsFree = 0;

    /**
     * @return (1 - getProcessCpuLoad ()) * getProcessorCount()
     */
    public synchronized static double getProcessorsFree() {
        if (System.currentTimeMillis() - lastCheckedProcessorsFree > UPDATE_INTERVAL) {
            processorsFree = getProcessorCount() * (1 - getProcessCpuLoad());
            lastCheckedProcessorsFree = System.currentTimeMillis();
        }
        return processorsFree;
    }
}
