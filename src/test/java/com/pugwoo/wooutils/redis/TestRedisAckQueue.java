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
    public void testCleanTopic() {
        redisHelper.removeTopic("mytopic5");
    }

}
