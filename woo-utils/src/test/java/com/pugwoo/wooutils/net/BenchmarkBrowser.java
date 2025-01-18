package com.pugwoo.wooutils.net;

import com.pugwoo.wooutils.thread.ThreadPoolUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

public class BenchmarkBrowser {

    public static void main(String[] args) throws Exception {
        Browser browser = new Browser();

        AtomicInteger total = new AtomicInteger();
        AtomicInteger fail = new AtomicInteger();

        ThreadPoolExecutor executeThem = ThreadPoolUtils.createThreadPool(100, 10000, 100, "test");

        List<Future<String>> futures = new ArrayList<>();
        for(int i = 0; i < 100; i++) {
            futures.add(executeThem.submit(() -> {
                for(int i1 = 0; i1 < 10000; i1++) {
                    try {
                        HttpResponse resp = browser.get("http://10.100.99.41:38667/");
                        total.incrementAndGet();
                    } catch (Exception e) {
                        fail.incrementAndGet();
                        e.printStackTrace();
                    }
                }
                return "done";
            }));
        }

        final AtomicInteger last = new AtomicInteger();
        Thread log = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    int _total = total.get();
                    System.out.println((_total - last.get()) + " requests/second");
                    last.set(_total);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                }
            }
        });
        log.setDaemon(true);
        log.start();

        ThreadPoolUtils.waitAllFuturesDone(futures);

        System.out.println("fail:" + fail.get());

    }

}
