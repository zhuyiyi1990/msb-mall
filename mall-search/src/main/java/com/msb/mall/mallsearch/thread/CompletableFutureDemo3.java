package com.msb.mall.mallsearch.thread;

import java.util.concurrent.*;

public class CompletableFutureDemo3 {

    private static ThreadPoolExecutor executor = new ThreadPoolExecutor(5
            , 50
            , 10
            , TimeUnit.SECONDS
            , new LinkedBlockingQueue<>(100)
            , Executors.defaultThreadFactory()
            , new ThreadPoolExecutor.AbortPolicy()
    );

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("线程开始了..." + Thread.currentThread().getName());
            int i = 100 / 5;
            System.out.println("线程结束了..." + Thread.currentThread().getName());
            return i;
        }, executor).thenApply(res -> {
            System.out.println("res = " + res);
            return res * 100;
        });
        // 可以处理异步任务之后的操作
        System.out.println("获取的线程的返回结果是：" + future.get());
    }

    /*public static void main(String[] args) throws ExecutionException, InterruptedException {
        CompletableFuture<Void> voidCompletableFuture = CompletableFuture.supplyAsync(() -> {
            System.out.println("线程开始了..." + Thread.currentThread().getName());
            int i = 100 / 5;
            System.out.println("线程结束了..." + Thread.currentThread().getName());
            return i;
        }, executor).thenAcceptAsync(res -> {
            System.out.println(res + ":" + Thread.currentThread().getName());
        }, executor);
        // 可以处理异步任务之后的操作
        //System.out.println("获取的线程的返回结果是：" + future.get());
    }*/

    /*public static void main(String[] args) throws ExecutionException, InterruptedException {
        CompletableFuture<Void> voidCompletableFuture = CompletableFuture.supplyAsync(() -> {
            System.out.println("线程开始了..." + Thread.currentThread().getName());
            int i = 100 / 5;
            System.out.println("线程结束了..." + Thread.currentThread().getName());
            return i;
        }, executor).thenRunAsync(() -> {
            System.out.println("线程开始了..." + Thread.currentThread().getName());
            int i = 100 / 5;
            System.out.println("线程结束了..." + Thread.currentThread().getName());
        }, executor);
        // 可以处理异步任务之后的操作
        //System.out.println("获取的线程的返回结果是：" + future.get());
    }*/

}