package com.pugwoo.wooutils.net;

import com.pugwoo.wooutils.task.ExecuteThem;

import java.util.concurrent.atomic.AtomicInteger;

public class BenchmarkBrowser {

    public static void main(String[] args) throws Exception {
        Browser browser = new Browser();

        AtomicInteger total = new AtomicInteger();
        AtomicInteger fail = new AtomicInteger();

        ExecuteThem executeThem = new ExecuteThem(100);

        for(int i = 0; i < 100; i++) {
            executeThem.add(new Runnable() {
                @Override
                public void run() {
                    for(int i = 0; i < 10000; i++) {
                        try {
                            HttpResponse resp = browser.get("http://10.100.99.41:38667/");
                            total.incrementAndGet();
                        } catch (Exception e) {
                            fail.incrementAndGet();
                            e.printStackTrace();
                        }
                    }
                }
            });
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

        executeThem.waitAllTerminate();

        System.out.println("fail:" + fail.get());

    }

}
