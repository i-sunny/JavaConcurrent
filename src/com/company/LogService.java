package com.company;

import java.io.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by sunny on 16/3/15.
 */
//向LogWriter添加可靠的取消操作
public class LogService {
    private final BlockingQueue<String> queue;
    private final LoggerThread loggerThread;
    private final PrintWriter writer;
    private boolean isShutdown;
    private int reservtions;
    private static final int CAPACITY = 100;

    public LogService(PrintWriter writer) {
        this.writer = writer;
        queue = new LinkedBlockingQueue<>(CAPACITY);
        loggerThread = new LoggerThread();
    }

    public void start(){
        loggerThread.start();
    }

    //自建的stop 能够关闭消费者的同时使生产者关闭
    public void stop(){
        synchronized (this){ isShutdown = true; }
        loggerThread.interrupt();
    }

    public void log(String msg) throws InterruptedException {
        synchronized (this) {
            if (isShutdown)
                throw new IllegalStateException();
            reservtions++;
        }
            queue.put(msg);
    }

    private class LoggerThread extends Thread {
        @Override
        public void run() {
            try {
                while (true) {
                    try {
                        synchronized (LogService.this){
                            if (isShutdown && reservtions == 0)
                                break;
                        }
                        String msg = queue.take();
                        synchronized (LogService.this){ reservtions--; }
                        writer.println(msg);
                    }catch (InterruptedException e) { /*retry*/ }
                }
            }finally {
                writer.close();
            }
        }
    }

    public static void main(String[] args) throws IOException{
        File file = new File("log.txt");
        LogService logService = new LogService(new PrintWriter(new BufferedWriter(new FileWriter(file))));
        try {
            logService.start();
            for (int i = 0; i < 50; i++){
                logService.log("This is line " + i);
            }
            logService.stop();
        } catch (InterruptedException e) { }
    }
}
