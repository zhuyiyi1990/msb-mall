package com.msb.mall.mallsearch.thread;

import java.util.concurrent.*;

public class CompletableFutureDemo4 {

    private static ThreadPoolExecutor executor = new ThreadPoolExecutor(5
            , 50
            , 10
            , TimeUnit.SECONDS
            , new LinkedBlockingQueue<>(100)
            , Executors.defaultThreadFactory()
            , new ThreadPoolExecutor.AbortPolicy()
    );

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务1 线程开始了..." + Thread.currentThread().getName());
            int i = 100 / 5;
            System.out.println("任务1 线程结束了..." + Thread.currentThread().getName());
            return i;
        }, executor);
        CompletableFuture<Integer> future2 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务2 线程开始了..." + Thread.currentThread().getName());
            int i = 100 / 10;
            System.out.println("任务2 线程结束了..." + Thread.currentThread().getName());
            return i;
        }, executor);

        CompletableFuture<Void> voidCompletableFuture = future1.runAfterBothAsync(future2, () -> {
            System.out.println("任务3执行了");
        }, executor);

        CompletableFuture<Void> voidCompletableFuture1 = future1.thenAcceptBothAsync(future2, (f1, f2) -> {
            System.out.println("f1 = " + f1);
            System.out.println("f2 = " + f2);
        }, executor);

        CompletableFuture<String> stringCompletableFuture = future1.thenCombineAsync(future2, (f1, f2) -> {
            return f1 + ":" + f2;
        }, executor);

        // 可以处理异步任务之后的操作
        System.out.println("获取的线程的返回结果是：" + stringCompletableFuture.get());
    }

}