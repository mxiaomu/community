package com.maple.community;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 线程池
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class ThreadPoolTest {

    //
    private static final Logger logger =
            LoggerFactory.getLogger(ThreadPoolTest.class);

    // JDK 普通线程池
    private ExecutorService executorService = Executors.newFixedThreadPool(5);

    // JDK 定时任务线程池
    private ScheduledExecutorService scheduledExecutorService =
            Executors.newScheduledThreadPool(5);
    // spring 普通线程池
    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    // spring 定时任务线程池
    @Autowired
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;

    private void sleep(long m) throws InterruptedException {
        Thread.sleep(m);
    }
    @Test
    public void testExecutorService() throws InterruptedException {
        Runnable task = () -> logger.info("hello world");
        for(int i = 0 ; i < 10 ; i++){
            executorService.submit(task);
        }
        sleep(10000);
    }

    @Test
    public void testSchedulerExecutorService() throws InterruptedException {
        Runnable task = ()->logger.info("hello schedulerExecutorService");
        scheduledExecutorService.scheduleAtFixedRate(task,10000,
                1000,TimeUnit.MILLISECONDS);
        sleep(30000);
    }

    @Test
    public void testThreadPoolTaskExecutor() throws InterruptedException {
        Runnable task = ()->logger.info("hello threadPoolTaskExecutor");
        for(int i = 0; i < 10; i++){
            threadPoolTaskExecutor.submit(task);
        }
        sleep(10000);
    }

    @Test
    public void testThreadPoolTaskScheduler() throws InterruptedException {
        Runnable task = ()->logger.info("hello threadPoolTaskScheduler");
        threadPoolTaskScheduler.scheduleAtFixedRate(task,new Date(System.currentTimeMillis() + 10000),
                1000);
        sleep(30000);
    }


}
