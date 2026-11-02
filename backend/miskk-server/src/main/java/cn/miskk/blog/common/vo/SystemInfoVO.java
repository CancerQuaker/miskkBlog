package cn.miskk.blog.common.vo;

import lombok.Data;

import java.util.List;

/**
 * 系统监控信息（oshi）。
 */
@Data
public class SystemInfoVO {

    /** CPU 使用率（%） */
    private double cpuLoad;
    private int cpuCores;
    private List<Double> perCoreLoad;

    /** 内存（MB） */
    private long memTotal;
    private long memUsed;
    /** 内存使用率（%） */
    private double memUsage;

    /** JVM 内存（MB） */
    private long jvmMax;
    private long jvmTotal;
    private long jvmUsed;

    private List<DiskInfo> disks;

    private String osName;
    private String osArch;
    private String javaVersion;
    /** 系统运行时长（秒） */
    private long uptimeSeconds;

    @Data
    public static class DiskInfo {
        private String name;
        private long total;
        private long used;
        private double usage;
    }
}
