package com.cloudspace.jindun.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TaskUtil {
    /**
     * 回调接口
     * 当某个方法执行完毕之后进行回调
     */
    interface TaskCallback {
        void onComplete(boolean success);
    }

    private static ExecutorService singleExecutor = Executors.newSingleThreadExecutor();

    private static ExecutorService threadPoolExecutor = Executors.newCachedThreadPool();

    private static ScheduledExecutorService schedulerExecutor = Executors.newSingleThreadScheduledExecutor();

    public static final void executeTask(Runnable run) {
        threadPoolExecutor.execute(run);
    }

    public static final void executeSingleTask(Runnable run) {
        singleExecutor.execute(run);
    }

    public static final void executeScheduleTask(Runnable run, int delay, TimeUnit unit) {
        schedulerExecutor.schedule(run, delay, unit);
    }


}
