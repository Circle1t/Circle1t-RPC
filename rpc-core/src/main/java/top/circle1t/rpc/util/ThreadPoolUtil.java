package top.circle1t.rpc.util;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.*;

/**
 * 线程池工具类
 * 作用：统一创建和管理线程池，避免重复创建线程池造成的资源浪费
 * 特点：提供CPU密集型、IO密集型等常见场景的线程池快捷创建方法，并通过缓存复用线程池
 * @author Circle1t
 * @since 2025/10/30
 */
@Slf4j
public final class ThreadPoolUtil { // final修饰类，禁止被继承（工具类通常不希望被继承）

    /**
     * 线程池缓存容器
     * 用ConcurrentHashMap（线程安全的Map）存储已创建的线程池，键为线程池名称，值为线程池实例
     * 作用：通过名称复用线程池，避免重复创建（线程池是重量级资源，频繁创建销毁会消耗性能）
     */
    private static final Map<String, ExecutorService> THREAD_POOL_CACHE = new ConcurrentHashMap<>();

    /**
     * 可用CPU核心数
     * 通过Runtime获取当前设备的CPU核心数量，是线程池参数设置的重要依据
     */
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();

    /**
     * CPU密集型任务的核心线程数（CPU核心数 + 1）
     * 原理：CPU密集型任务（如计算）主要消耗CPU资源，线程数过多会导致线程切换频繁（浪费CPU）
     * 经验值：线程数=CPU核心数+1，既能充分利用CPU，又不会因切换导致过多开销
     */
    private static final int CPU_INCENTIVE_COUNT = CPU_COUNT + 1;

    /**
     * IO密集型任务的核心线程数（CPU核心数 * 2）
     * 原理：IO密集型任务（如网络请求、文件读写）会频繁等待IO响应（此时线程空闲）
     * 经验值：线程数=CPU核心数*2，用更多线程利用空闲时间，提高任务处理效率
     */
    private static final int IO_INCENTIVE_COUNT = CPU_COUNT * 2;

    /**
     * 默认空闲线程存活时间（60秒）
     * 作用：当线程池中的线程数超过核心线程数时，多余的空闲线程等待新任务的最长时间
     * 超过这个时间仍无任务，多余线程会被销毁，节省资源
     */
    private static final int DEFAULT_KEEP_ALIVE_TIME = 60;

    /**
     * 默认任务队列容量（128）
     * 作用：线程池的任务缓冲队列，当所有核心线程都在工作时，新任务会先进入队列等待
     */
    private static final int DEFAULT_QUEUE_SIZE = 128;

    /**
     * 创建CPU密集型线程池
     * @param poolName 线程池名称（用于标识和缓存线程池）
     * @return 构建好的线程池实例
     */
    public static ExecutorService createCPUIncentiveThreadPool(String poolName) {
        // 调用重载方法，核心线程数和最大线程数都使用CPU密集型的推荐值
        return createThreadPool(CPU_INCENTIVE_COUNT, poolName);
    }

    /**
     * 创建IO密集型线程池
     * @param poolName 线程池名称（用于标识和缓存线程池）
     * @return 构建好的线程池实例
     */
    public static ExecutorService createIoIncentiveThreadPool(String poolName) {
        // 调用重载方法，核心线程数和最大线程数都使用IO密集型的推荐值
        return createThreadPool(IO_INCENTIVE_COUNT, poolName);
    }

    /**
     * 创建线程池（核心线程数=最大线程数）
     * @param corePoolSize 核心线程数（线程池长期保持的线程数量，即使空闲也不会销毁）
     * @param poolName 线程池名称
     * @return 构建好的线程池实例
     */
    public static ExecutorService createThreadPool(int corePoolSize, String poolName) {
        // 核心线程数和最大线程数相同（固定大小的线程池）
        return createThreadPool(corePoolSize, corePoolSize, poolName);
    }

    /**
     * 创建线程池（指定核心线程数和最大线程数）
     * @param corePoolSize 核心线程数
     * @param maxPoolSize 最大线程数（线程池允许的最大线程数量，当队列满时会创建新线程直到达到该值）
     * @param poolName 线程池名称
     * @return 构建好的线程池实例
     */
    public static ExecutorService createThreadPool(int corePoolSize, int maxPoolSize, String poolName) {
        // 调用最完整的构建方法，使用默认的存活时间、队列大小，非守护线程
        return createThreadPool(corePoolSize, maxPoolSize, DEFAULT_KEEP_ALIVE_TIME, DEFAULT_QUEUE_SIZE, poolName,false);
    }

    /**
     * 最完整的线程池创建方法（自定义所有核心参数）
     * @param corePoolSize 核心线程数
     * @param maxPoolSize 最大线程数
     * @param keepAliveTime 空闲线程存活时间（当线程数超过核心线程数时）
     * @param queueSize 任务队列容量
     * @param poolName 线程池名称（用于缓存和线程命名）
     * @param isDaemon 是否为守护线程（守护线程：当主线程结束时，守护线程会自动终止，如垃圾回收线程）
     * @return 构建好的线程池实例（从缓存获取或新创建）
     */
    public static ExecutorService createThreadPool(
            int corePoolSize,
            int maxPoolSize,
            long keepAliveTime,
            int queueSize,
            String poolName,
            boolean isDaemon
    ) {
        // 先检查缓存中是否已存在该名称的线程池，存在则直接返回（复用）
        if(THREAD_POOL_CACHE.containsKey(poolName)) {
            return THREAD_POOL_CACHE.get(poolName);
        }

        // 若缓存中没有，创建新的线程池（ThreadPoolExecutor是Java线程池的核心实现类）
        ExecutorService executorService = new ThreadPoolExecutor(
                corePoolSize, // 核心线程数
                maxPoolSize, // 最大线程数
                keepAliveTime, // 空闲线程存活时间
                TimeUnit.SECONDS, // 存活时间的单位（这里是秒）
                new ArrayBlockingQueue<>(queueSize), // 任务队列（有界队列，容量为queueSize，避免内存溢出）
                createThreadFactory(poolName, isDaemon) // 线程工厂（用于创建线程，统一命名便于调试）
                // 注意：这里省略了拒绝策略参数，默认使用AbortPolicy（任务满时抛出异常）
        );

        log.info("创建线程池成功！线程池名称：{}", poolName);

        // 将新创建的线程池放入缓存，供后续复用
        THREAD_POOL_CACHE.put(poolName, executorService);
        return executorService;
    }

    /**
     * 创建线程工厂（默认非守护线程）
     * 线程工厂：用于统一创建线程，规范线程命名、优先级等属性，便于问题排查
     * @param poolName 线程池名称（用于线程命名前缀）
     * @return 线程工厂实例
     */
    public static ThreadFactory createThreadFactory(String poolName) {
        // 调用重载方法，默认创建非守护线程
        return createThreadFactory(poolName, false);
    }

    /**
     * 创建线程工厂（可指定是否为守护线程）
     * @param poolName 线程池名称（用于线程命名前缀，如"rpc-pool-"）
     * @param isDaemon 是否为守护线程
     * @return 线程工厂实例
     */
    public static ThreadFactory createThreadFactory(String poolName, boolean isDaemon) {
        // 使用Hutool的ThreadFactoryBuilder构建线程工厂
        ThreadFactoryBuilder threadFactoryBuilder = ThreadFactoryBuilder.create()
                .setDaemon(isDaemon); // 设置是否为守护线程

        // 若线程池名称不为空，设置线程名称前缀（如"rpc-pool-1"、"rpc-pool-2"）
        if(StrUtil.isBlank(poolName)) {
            return threadFactoryBuilder.build(); // 无名称前缀的线程工厂
        }
        return threadFactoryBuilder.setNamePrefix(poolName).build(); // 带名称前缀的线程工厂
    }

     public static void shutdownAll() {
        THREAD_POOL_CACHE.entrySet().parallelStream().forEach(entry -> {
            String key = entry.getKey();
            ExecutorService executorService = entry.getValue();

            executorService.shutdown();
            log.info("线程池：{} 正在关闭", key);

            try {
                if(executorService.awaitTermination(10, TimeUnit.SECONDS)){
                    log.info("线程池：{} 关闭成功", key);
                } else {
                    log.info("线程池：{} 10秒内未关闭，正在强制关闭", key);
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e){
                log.error("线程池：{} 关闭异常", key);
                executorService.shutdownNow();
            }
        });
    }

}