package com.msb.mall.mallsearch.thread;

import java.util.concurrent.*;

public class CompletableFutureDemo5 {

    private static ThreadPoolExecutor executor = new ThreadPoolExecutor(5
            , 50
            , 10
            , TimeUnit.SECONDS
            , new LinkedBlockingQueue<>(100)
            , Executors.defaultThreadFactory()
            , new ThreadPoolExecutor.AbortPolicy()
    );

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        CompletableFuture<Object> future1 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务1 线程开始了..." + Thread.currentThread().getName());
            int i = 100 / 5;
            System.out.println("任务1 线程结束了..." + Thread.currentThread().getName());
            return i;
        }, executor);
        CompletableFuture<Object> future2 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务2 线程开始了..." + Thread.currentThread().getName());
            int i = 100 / 10;
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("任务2 线程结束了..." + Thread.currentThread().getName());
            return i + "";
        }, executor);

        future1.runAfterEitherAsync(future2, () -> {
            System.out.println("任务3执行了....");
        }, executor);

        future1.acceptEitherAsync(future2, (res) -> {
            System.out.println("res = " + res);
        }, executor);

        CompletableFuture<String> stringCompletableFuture = future1.applyToEitherAsync(future2, (res) -> {
            System.out.println("res = " + res);
            return res + "-->OK";
        }, executor);
        // 可以处理异步任务之后的操作
        System.out.println("获取的线程的返回结果是：" + stringCompletableFuture.get());
    }

}