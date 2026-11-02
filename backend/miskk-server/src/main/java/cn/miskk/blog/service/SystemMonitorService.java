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

    /** 上一轮 CPU ticks 基线：跨调用按轮询间隔计算平均使用率 */
    private long[] prevSystemTicks;
    private long[][] prevPerCoreTicks;

    public synchronized SystemInfoVO collect() {
        HardwareAbstractionLayer hal = systemInfo.getHardware();
        OperatingSystem os = systemInfo.getOperatingSystem();

        SystemInfoVO vo = new SystemInfoVO();

        // CPU：与上一轮采集点比较，窗口即前端轮询间隔（约 5 秒），等价于任务管理器的滚动平均。
        // Windows 下 200ms 级短窗口采样恒为 0（tick 量化），故仅服务启动后首轮无基线时用 300ms 短间隔兜底
        CentralProcessor cpu = hal.getProcessor();
        vo.setCpuCores(cpu.getLogicalProcessorCount());
        boolean first = prevSystemTicks == null;
        long[] sysBaseline = first ? cpu.getSystemCpuLoadTicks() : prevSystemTicks;
        long[][] coreBaseline = first ? cpu.getProcessorCpuLoadTicks() : prevPerCoreTicks;
        if (first) {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        double load = cpu.getSystemCpuLoadBetweenTicks(sysBaseline) * 100;
        vo.setCpuLoad(Math.max(0, Math.min(100, load)));
        List<Double> cores = new ArrayList<>();
        for (double l : cpu.getProcessorCpuLoadBetweenTicks(coreBaseline)) {
            cores.add(Math.max(0, Math.min(100, l * 100)));
        }
        vo.setPerCoreLoad(cores);
        // 本轮时刻作为下一轮基线
        prevSystemTicks = cpu.getSystemCpuLoadTicks();
        prevPerCoreTicks = cpu.getProcessorCpuLoadTicks();

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
