package cn.miskk.blog.service;

import cn.miskk.blog.common.vo.SystemInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;
import oshi.software.os.OSFileStore;

import java.util.ArrayList;
import java.util.List;

/**
 * 系统监控服务（基于 oshi）。
 */
@Slf4j
@Service
public class SystemMonitorService {

    private final SystemInfo systemInfo = new SystemInfo();

    public SystemInfoVO collect() {
        HardwareAbstractionLayer hal = systemInfo.getHardware();
        OperatingSystem os = systemInfo.getOperatingSystem();

        SystemInfoVO vo = new SystemInfoVO();

        // CPU：两次采样间隔内计算使用率
        CentralProcessor cpu = hal.getProcessor();
        vo.setCpuCores(cpu.getLogicalProcessorCount());
        long[] startTicks = cpu.getSystemCpuLoadTicks();
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        double load = cpu.getSystemCpuLoadBetweenTicks(startTicks) * 100;
        vo.setCpuLoad(Math.max(0, Math.min(100, load)));
        List<Double> cores = new ArrayList<>();
        long[][] startPerProc = cpu.getProcessorCpuLoadTicks();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        for (double l : cpu.getProcessorCpuLoadBetweenTicks(startPerProc)) {
            cores.add(Math.max(0, Math.min(100, l * 100)));
        }
        vo.setPerCoreLoad(cores);

        // 物理内存
        long memTotal = hal.getMemory().getTotal();
        long memAvailable = hal.getMemory().getAvailable();
        vo.setMemTotal(memTotal / 1024 / 1024);
        vo.setMemUsed((memTotal - memAvailable) / 1024 / 1024);
        vo.setMemUsage(memTotal == 0 ? 0 : (memTotal - memAvailable) * 100.0 / memTotal);

        // JVM 内存
        Runtime runtime = Runtime.getRuntime();
        vo.setJvmMax(runtime.maxMemory() / 1024 / 1024);
        vo.setJvmTotal(runtime.totalMemory() / 1024 / 1024);
        vo.setJvmUsed((runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024);

        // 磁盘分区
        List<SystemInfoVO.DiskInfo> disks = new ArrayList<>();
        for (OSFileStore store : os.getFileSystem().getFileStores()) {
            try {
                SystemInfoVO.DiskInfo disk = new SystemInfoVO.DiskInfo();
                disk.setName(store.getName() + " (" + store.getMount() + ")");
                long total = store.getTotalSpace();
                long usable = store.getUsableSpace();
                if (total <= 0) {
                    continue;
                }
                disk.setTotal(total / 1024 / 1024 / 1024);
                disk.setUsed((total - usable) / 1024 / 1024 / 1024);
                disk.setUsage((total - usable) * 100.0 / total);
                disks.add(disk);
            } catch (Exception ignored) {
                // 个别挂载点无法读取，跳过
            }
        }
        vo.setDisks(disks);

        vo.setOsName(os.getFamily() + " " + os.getVersionInfo().getVersion());
        vo.setOsArch(System.getProperty("os.arch"));
        vo.setJavaVersion(System.getProperty("java.version"));
        vo.setUptimeSeconds((long) os.getSystemUptime());
        return vo;
    }
}
