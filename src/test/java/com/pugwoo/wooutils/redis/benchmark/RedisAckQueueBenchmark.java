package com.pugwoo.wooutils.redis.benchmark;

import com.pugwoo.wooutils.json.JSON;
import com.pugwoo.wooutils.redis.RedisHelper;
import com.pugwoo.wooutils.redis.RedisMsg;
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
public class RedisAckQueueBenchmark {

    @Autowired
    private RedisHelper redisHelper;

    // 两个压测对同一个topic同时进行
    @Test
    public void benchCheck2() {
        benchCheck();
    }

    @Test
    public void benchCheck() {

        final String topic = "benchmarkQueue";
        final int SENDER = 3; // 并发发送者个数
        final int PER_SEND_ROUND = 100; // 每个ROUND发送100条，有单个发和批量发的组合，所以即使发送就是 SEND*PER_SEND_ROUND*100条
        final int RECEIVER = 30; // 并发接收者
        final int NACK_PERCENT = 13; // 接收者nack的比例

        Map<String, String> map = new ConcurrentHashMap<>();
        ExecuteThem executeThem = new ExecuteThem(SENDER + RECEIVER);

        AtomicLong totalSend = new AtomicLong();
        AtomicLong lastSend = new AtomicLong();
        AtomicLong totalRecv = new AtomicLong();
        AtomicLong lastRecv = new AtomicLong();
        AtomicLong sendErrorCount = new AtomicLong();
        AtomicLong nackCount = new AtomicLong();
        AtomicLong valueNotMatch = new AtomicLong();

        // 总发送：SEND*PER_SEND_ROUND*100条
        for(int i = 0; i < SENDER; i++) {
            executeThem.add(new Runnable() {
                @Override
                public void run() {
                    for(int j = 0; j < PER_SEND_ROUND; j++) {
                        // 每个round发送100条，其中15条是单个发的
                        for (int i = 0; i < 15; i++) {
                            String msg = UUID.randomUUID().toString();
                            String uuid = redisHelper.send(topic, msg);
                            if(uuid ==null) {
                                sendErrorCount.incrementAndGet();
                            } else {
                                totalSend.incrementAndGet();
                                map.put(uuid, msg);
                            }
                        }

                        // 批量发送的分别是25条和60条
                        List<String> batch25 = new ArrayList<>();
                        List<String> batch60 = new ArrayList<>();

                        for (int i = 0; i < 25; i++) {
                            batch25.add(UUID.randomUUID().toString());
                        }
                        for (int i = 0; i < 60; i++) {
                            batch60.add(UUID.randomUUID().toString());
                        }

                        List<String> uuid25 = redisHelper.sendBatch(topic, batch25);
                        List<String> uuid60 =redisHelper.sendBatch(topic, batch60);

                        if (uuid25.size() != 25) {
                            sendErrorCount.addAndGet(25 - uuid25.size());
                        }
                        if (uuid60.size() != 60) {
                            sendErrorCount.addAndGet(60 - uuid60.size());
                        }

                        for (int i = 0; i < 25; i++) {
                            totalSend.incrementAndGet();
                            map.put(uuid25.get(i), batch25.get(i));
                        }
                        for (int i = 0; i < 60; i++) {
                            totalSend.incrementAndGet();
                            map.put(uuid60.get(i), batch60.get(i));
                        }
                    }
                }
            });
        }

        // 模拟RECEIVER个接收者
        for(int i = 0; i < RECEIVER; i++) {
            executeThem.add(new Runnable() {
                @Override
                public void run() {
                    while(true) {
                        RedisMsg msg = redisHelper.receive(topic, 10, null);
                        if(msg == null) {
                            break; // 接收不到就退出
                        }
                        // 因为发送方发送完消息后，可能还没来得及放到map，就被消费了，所以这里可能map没有删除掉
                        // 但是这个概率非常非常低

                        // 这里模拟一定比例的nack
                        if(new Random().nextInt(100) < NACK_PERCENT) {
                            redisHelper.nack(topic, msg.getUuid());
                            nackCount.incrementAndGet();
                        } else {
                            String valueInMap = map.remove(msg.getUuid());
                            if (!msg.getMsg().equals(valueInMap)) {
                                valueNotMatch.incrementAndGet();
                            }
                            redisHelper.ack(topic, msg.getUuid());
                            totalRecv.incrementAndGet();
                        }
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

        // 打印和assert结果
        System.out.println("总发送:" + totalSend.get() + ",发送失败:" + sendErrorCount.get());
        System.out.println("总接收:" + totalRecv.get() + ",发生nack数:" + nackCount.get() + ",内容不匹配:" + valueNotMatch.get());
        System.out.println("map比对结果:" + JSON.toJson(map.keySet()));

    }

}
