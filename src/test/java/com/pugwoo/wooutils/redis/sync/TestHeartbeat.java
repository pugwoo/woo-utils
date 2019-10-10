package com.pugwoo.wooutils.redis.sync;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration(locations = {"classpath:applicationContext-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class TestHeartbeat {

    @Autowired
    private HeartbeatTestService heartbeatTestService;

    @Test
    public void test() throws Exception {

        // 起3个线程跑，每个跑30秒，共90秒多点
        for(int i = 0; i < 3; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    heartbeatTestService.longTask();
                }
            }).start();
        }

        Thread.sleep(150000); // 主线程等待子线程执行结束，给够150秒
    }

}
