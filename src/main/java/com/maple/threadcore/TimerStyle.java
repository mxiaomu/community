package com.maple.threadcore;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 使用计时器方式创建线程
 */
public class TimerStyle {
    public static void main(String[] args) {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                System.out.println(Thread.currentThread().getName());
            }
        },1000, 1000);
    }
}
