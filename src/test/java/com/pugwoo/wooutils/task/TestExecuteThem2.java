package com.pugwoo.wooutils.task;

import java.util.concurrent.atomic.AtomicLong;

public class TestExecuteThem2 {

    public static void main(String[] args) {
        ExecuteThem executeThem = new ExecuteThem(10);

        final AtomicLong total = new AtomicLong(0);
        final AtomicLong done = new AtomicLong(0);

        new Thread(new Runnable() {
            @Override
            public void run() {
                for(int i = 0; i < 100000000; i++) {
                    executeThem.add(new Runnable() {
                        @Override
                        public void run() {
                            int all = 0;
                            for(int j = 0; j < 1000; j++) {
                                all += j;
                            }
                            try {
                                Thread.sleep(100000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            done.incrementAndGet();
                        }
                    });
                    total.incrementAndGet();
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();


        while(true) {
            System.out.println("total:" + total.get() + ",done:" + done.get());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


    }

}
