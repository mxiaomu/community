package com.maple.threadcore;


/**
 * 使用 Lambda 表达式创建线程
 */
public class LambdaStyle {

    public static void main(String[] args) {
        new Thread(()-> System.out.println(Thread.currentThread().getName())).start();
    }

}