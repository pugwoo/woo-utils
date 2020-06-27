package com.pugwoo.wooutils.redis;


import com.pugwoo.wooutils.collect.ListUtils;
import com.pugwoo.wooutils.json.JSON;
import com.pugwoo.wooutils.task.ExecuteThem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@ContextConfiguration(locations = {"classpath:applicationContext-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class TestRedisAckQueue {

    @Autowired
    private RedisHelper redisHelper;
    
    @Test
    public void testSendOne() {
        String uuid = redisHelper.send("mytopic2", "msgconent" + UUID.randomUUID().toString());
        System.out.println("send msg:" + uuid);
    }
    
    @Test
    public void testRecvAckOne() {
        RedisMsg msg = redisHelper.receive("mytopic2");
        if(msg == null) {
            return;
        }
        System.out.println("revc msg ack uuid:" + msg.getUuid() + ",content:" + msg.getMsg());
        redisHelper.ack("mytopic2", msg.getUuid());
    }
    
    @Test
    public void testRecvNackOne() {
        RedisMsg msg = redisHelper.receive("mytopic2");
        if (msg == null) {
            return;
        }
        System.out.println("revc msg nack uuid:" + msg.getUuid() + ",content:" + msg.getMsg());
        redisHelper.nack("mytopic2", msg.getUuid());
    }
    
    @Test
    public void testSendBatch() {
        List<String> msgList4 = ListUtils.newArrayList(
                "msgconent" + UUID.randomUUID().toString(),
                "msgconent" + UUID.randomUUID().toString(),
                "msgconent" + UUID.randomUUID().toString()
        );
        List<String> uuidList4 = redisHelper.sendBatch("mytopic4", msgList4);
        System.out.println("send msgList:" + uuidList4);
    
        List<String> msgList5 = ListUtils.newArrayList(
                "msgconent" + UUID.randomUUID().toString(),
                "msgconent" + UUID.randomUUID().toString(),
                "msgconent" + UUID.randomUUID().toString()
        );
        List<String> uuidList5 = redisHelper.sendBatch("mytopic5", msgList5, 60);
        System.out.println("send msgList:" + uuidList5);
    }
    
    @Test
    public void testSendRepeatedly() {

        while(true) {
            String uuid = redisHelper.send("mytopic1", "msgconent" + UUID.randomUUID().toString());
            System.out.println("send msg:" + uuid);

            //if(uuid == null) {
            //    System.out.println("uuid is null");
            //}

            try {
                Thread.sleep(new Random().nextInt(1000));
            } catch (Exception e) {
            }
        }

    }

    @Test
    public void testRecvAckRepeatedly() {

        while(true) {
            RedisMsg msg = redisHelper.receive("mytopic1");
            if(msg == null) {
                continue;
            }
            System.out.println("revc msg ack uuid:" + msg.getUuid() + ",content:" + msg.getMsg());

            try {
                Thread.sleep(new Random().nextInt(2000));
            } catch (Exception e) {
            }

            redisHelper.ack("mytopic1", msg.getUuid());
        }
    }
    
    @Test
    public void testRecvNackRepeatedly() {
        
        while(true) {
            RedisMsg msg = redisHelper.receive("mytopic1");
            if(msg == null) {
                continue;
            }
            System.out.println("revc msg nack uuid:" + msg.getUuid() + ",content:" + msg.getMsg());
    
            try {
                Thread.sleep(new Random().nextInt(2000));
            } catch (Exception e) {
            }
            
            redisHelper.nack("mytopic1", msg.getUuid());
        }
    }
    
    @Test
    public void benchCheck2() {
        benchCheck();
    }


    @Test
    public void benchCheck() {

        final String topic = "aaa";

        Map<String, String> map = new ConcurrentHashMap<>();
        ExecuteThem executeThem = new ExecuteThem(400);

        AtomicLong totalSend = new AtomicLong();
        AtomicLong lastSend = new AtomicLong();
        AtomicLong totalRecv = new AtomicLong();
        AtomicLong lastRecv = new AtomicLong();

        // 模拟2个发送者，每个发送100000条
        for(int i = 0; i < 100; i++) {
            executeThem.add(new Runnable() {
                @Override
                public void run() {
                    for(int j = 0; j < 10000; j++) {
                        String uuid = redisHelper.send(topic, "aaaaaa");
                        if(uuid ==null) {
                            System.err.println("发送消息失败");
                        } else {
                            totalSend.incrementAndGet();
                            map.put(uuid, "");
                        }
                    }
                }
            });
        }

        // 模拟10个接收者
        for(int i = 0; i < 300; i++) {
            executeThem.add(new Runnable() {
                @Override
                public void run() {
                    while(true) {
                        RedisMsg msg = redisHelper.receive(topic, 10, null);
                        if(msg == null) {
                            break;
                        }
                        // System.out.println("recv:" + msg.getUuid());
                       // if(!map.containsKey(msg.getUuid())) {
                       //     // 因为发送方发送完消息后，可能还没来得及放到map，就被消费了，所以这里等一等
                       //     try {
                       //         Thread.sleep(1000);
                        //    } catch (Exception e) {
                        //    }
                        //}
                       // if(!map.containsKey(msg.getUuid())) {
                       //     System.err.println("map not contain key:" + msg.getUuid());
                       // }
                        map.remove(msg.getUuid());
                        redisHelper.ack(topic, msg.getUuid());
                        totalRecv.incrementAndGet();
                    }
                }
            });
        }

        Thread status = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    long sendRate = totalSend.get() - lastSend.get();
                    lastSend.set(totalSend.get());
                    long recvRate = totalRecv.get() - lastRecv.get();
                    lastRecv.set(totalRecv.get());

                    System.out.println("send total:" + totalSend.get()
                            + ",send rate:" + sendRate + "/s, recv total:" + totalRecv.get()
                            + ",recv rate:" + recvRate + "/s");
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        });
        status.setDaemon(true);
        status.start();

        executeThem.waitAllTerminate();

        System.out.println("结果:" + JSON.toJson(map.keySet()));
    }

    @Test
    public void testCleanTopic() {
        redisHelper.cleanTopic("mytopic1");
    }

}
