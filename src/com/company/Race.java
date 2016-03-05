package com.company;

import java.util.concurrent.CountDownLatch;

/**
 * Created by sunny on 16/3/5.
 */
//闭锁：作用想像一扇门，在闭锁到达结束状态之前，这扇门一直是关闭的，并且没有任何线程能通过，
//当到达结束状态时，这扇门允许所有线程通过（类似集齐几把钥匙能够开启大门)
//使用场景：
// 确保某个计算在其需要的所有资源都被初始化之后才继续执行
//确保某个服务在其依赖的所有其他服务都已经启动后才开始启动
public class Race {
    private final int numOfRunners;

    public Race(int numOfRunners) {
        this.numOfRunners = numOfRunners;
    }

    public static void main(String[] args) throws InterruptedException{
        Race race = new Race(10);

        final CountDownLatch startRace = new CountDownLatch(1);
        final CountDownLatch endRace = new CountDownLatch(race.numOfRunners);
        //创建各位运动员线程
        for (int i = 0; i < race.numOfRunners; i++){
            new Thread(){
                @Override
                public void run() {
                    try {
                        startRace.await();
                        try {
                            System.out.println(Thread.currentThread().getName() + ": Run to final!");
                        } finally {
                            endRace.countDown();   //该运动员跑到终点
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }

        long startTime = System.nanoTime();   //比赛开始时间
        System.out.println("Race Start!");
        startRace.countDown();    //开始比赛  保证所有运动员同时开始比赛

        endRace.await();
        System.out.println("Race End!");
        long endTime = System.nanoTime();   //比赛开始时间
        System.out.println("比赛总时长: " + (endTime - startTime));
    }
}
