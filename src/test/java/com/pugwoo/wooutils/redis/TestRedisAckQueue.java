package com.pugwoo.wooutils.redis;


import com.pugwoo.wooutils.json.JSON;
import com.pugwoo.wooutils.task.ExecuteThem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@ContextConfiguration(locations = {"classpath:applicationContext-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class TestRedisAckQueue {

    @Autowired
    private RedisHelper redisHelper;

    @Test
    public void test() {
        redisHelper.execute(jedis -> {
            Set<String> hkeys = jedis.hkeys("mytopic1:MQMSG");

            List<String> doing = jedis.lrange("mytopic1:MQDOING", 0, -1);

            List<String> list = jedis.lrange("mytopic1:MQLIST", 0, -1);

            for(String d : doing) {
                if(!hkeys.contains(d)) {
                    System.out.println("doing not exist:" + d);
                }
            }
            for(String d : list) {
                if(!hkeys.contains(d)) {
                    System.out.println("list not exist:" + d);
                }
            }

            for(String key : hkeys) {
                if(!key.startsWith("rmq")) {
                    System.out.println(key);
                }
                //System.out.println(key);
            }
            //System.out.println("hkeys:" + JSON.toJson(hkeys));
            return null;
        });
    }

    @Test
    public void testSend() {

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
    public void testRecv() {

        while(true) {
            RedisMsg msg = redisHelper.receive("mytopic1");
            if(msg == null) {
                continue;
            }
            System.out.println("revc msg uuid:" + msg.getUuid() + ",content:" + msg.getMsg());

            //try {
            //    Thread.sleep(new Random().nextInt(10));
            //} catch (Exception e) {
            //}

            redisHelper.ack("mytopic1", msg.getUuid());
        }
    }


    @Test
    public void benchCheck() {

        final String topic = "aaa";

        Map<String, String> map = new ConcurrentHashMap<>();

        ExecuteThem executeThem = new ExecuteThem(10);

        // 模拟5个发送者，每个发送10000条
        for(int i = 0; i < 5; i++) {
            executeThem.add(new Runnable() {
                @Override
                public void run() {
                    for(int j = 0; j < 10000; j++) {
                        String uuid = redisHelper.send(topic, "aaaaaa");
                        if(uuid ==null) {
                            System.err.println("发送消息失败");
                        } else {
                            map.put(uuid, "");
                        }
                    }
                }
            });
        }

        // 模拟5个接收者
        for(int i = 0; i < 5; i++) {
            executeThem.add(new Runnable() {
                @Override
                public void run() {
                    while(true) {
                        RedisMsg msg = redisHelper.receive(topic, 10, null);
                        if(msg == null) {
                            break;
                        }
                        //System.out.println("recv:" + msg.getUuid());
                        map.remove(msg.getUuid());
                        redisHelper.ack(topic, msg.getUuid());
                    }
                }
            });
        }

        executeThem.waitAllTerminate();

        System.out.println("结果:" + JSON.toJson(map.keySet()));
    }


}
