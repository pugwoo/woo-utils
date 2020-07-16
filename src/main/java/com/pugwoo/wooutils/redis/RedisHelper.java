package com.pugwoo.wooutils.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.Transaction;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

public interface RedisHelper {

	/**
	 * 获得redisHelper的对象转换器
	 * @return
	 */
	IRedisObjectConverter getRedisObjectConverter();

	/**
	 * 检查redis是否已经准备就绪，包括ip端口、密码等是否已经正确，服务器端是否已经正常ping-pong
	 * @return
	 */
	boolean isOk();

	/**
	 * 传入jedis，然后自行实现逻辑，最后会自动关闭jedis资源。
	 * 
	 * 该方法用于替换原来getJedisConnection方法
	 * 
	 * @param jedisToFunc
	 * @return 返回jedisToFunc的返回值
	 */
	<R> R execute(Function<Jedis, R> jedisToFunc);
	
	/**
	 * 按顺序执行pipeline，返回所有执行的结果列表
	 * @param pipeline
	 * @return
	 */
	List<Object> executePipeline(Consumer<Pipeline> pipeline);
	
	/**
	 * 执行redis事务，keys是需要watch的key
	 * @param transaction
	 * @param keys
	 * @return
	 */
	List<Object> executeTransaction(Consumer<Transaction> transaction, String ...keys);

	/**
	 * 重命名redis的key
	 * @param oldKey
	 * @param newKey 如果newKey已存在，会覆盖掉
	 * @return 除非抛出异常，否则认为成功；不处理oldKey不存在的情况，认为是成功
	 */
	boolean rename(String oldKey, String newKey);

	/**
	 * 设置字符串
	 * @param key
	 * @param expireSecond
	 * @param value
	 * @return
	 */
	boolean setString(String key, int expireSecond, String value);
	
	/**
	 * 设置对象
	 * @param key
	 * @param expireSecond
	 * @param value
	 * @return
	 */
	<T> boolean setObject(String key, int expireSecond, T value);

	/**
	 * 当key不存在时才写入，写入成功返回true，写入失败返回false
	 * @param key
	 * @param expireSecond
	 * @param value
	 * @return
	 */
	boolean setStringIfNotExist(String key, int expireSecond, String value);
	
	/**
	 * 设置key的超时时间
	 * @param key
	 * @param expireSecond
	 * @return
	 */
	boolean setExpire(String key, int expireSecond);
	
	/**
	 * 获取key剩余的有效时间，秒
	 * @param key
	 * @return 如果没有设置超时，返回-1；如果key不存在，返回-2；如果有异常，返回-999
	 */
	long getExpireSecond(String key);
	
	/**
	 * 获取字符串，不存在或redis连接不上返回null
	 * @param key
	 * @return
	 */
	String getString(String key);
	
	/**
	 * 获取对象，需要提供IRedisObjectConverter的实现对象
	 * @param key redis key
	 * @return
	 */
	<T> T getObject(String key, Class<T> clazz);

	/**
	 * 获取对象，需要提供IRedisObjectConverter的实现对象
	 *
	 * @param key redis key
	 * @param genericClass 支持泛型类
	 * @return
	 */
	<T> T getObject(String key, Class<T> clazz, Class<?> genericClass);

	/**
	 * 获取对象，需要提供IRedisObjectConverter的实现对象
	 *
	 * @param key redis key
	 * @param genericClass1 支持泛型类1
	 * @param genericClass2 支持泛型类2
	 * @return
	 */
	<T> T getObject(String key, Class<T> clazz, Class<?> genericClass1, Class<?> genericClass2);

	/**
	 * 通过keys批量获得redis的key和值
	 * @param keys
	 * @return 个数和顺序和keys一直，如果key不存在，则其值为null。整个命令操作失败则返回null
	 */
	List<String> getStrings(List<String> keys);
	
	/**
	 * 通过keys批量获得redis的key和值
	 * @param keys
	 * @return 个数和顺序和keys一直，如果key不存在，则其值为null。整个命令操作失败则返回null
	 */
	<T> List<T> getObjects(List<String> keys, Class<T> clazz);

	/**
	 * 使用scan的方式获得key的列表【建议少用，适用于非高频的定时任务中】<br>
	 *
	 * 如何判断scan已经结束：
	 * 1.【不是】跟进返回的keys是否为空来判断。
	 * 2.如果返回值中的cursor等于0，则表示scan已经结束；或者根据返回值里面的completeIteration属性判断是否已经scan完了
	 *
	 * @param cursor 上次查询的游标位置，第一次查询传null或空字符串
	 * @param pattern 匹配字符串
	 * @param count 实际返回的keys并不会刚好等于count值，可能多也可能少，甚至可能一条都没有
	 * @return 返回的是匹配到的redis keys
	 */
	ScanResult<String> getKeys(String cursor, String pattern, int count);
	
	/**
	 * 通过pattern获得redis的所有key。pattern格式详见https://redis.io/commands/keys
	 * 【重要：redis的keys对于大量key的情况有性能问题，应尽量少用keys；如果确实需要，请用scan代替】
	 * @param pattern
	 * @return 失败返回null
	 */
	@Deprecated
	Set<String> getKeys(String pattern);
	
	/**
	 * 获得redis满足pattern的所有key和值。pattern格式详见https://redis.io/commands/keys
	 * 【重要：redis的keys对于大量key的情况有性能问题，应尽量少用keys；如果确实需要，请用scan代替】
	 * @param pattern
	 * @return 失败返回null
	 */
	@Deprecated
	Map<String, String> getStrings(String pattern);
	
	/**
	 * 获得redis满足pattern的所有key和值。pattern格式详见https://redis.io/commands/keys
	 * 【重要：redis的keys对于大量key的情况有性能问题，应尽量少用keys；如果确实需要，请用scan代替】
	 * @param pattern
	 * @param clazz
	 * @return 失败返回null
	 */
	@Deprecated
	<T> Map<String, T> getObjects(String pattern, Class<T> clazz);
	
	/**
	 * 删除指定的key
	 * @param key
	 * @return
	 */
	boolean remove(String key);
	
	/**
	 * key-value匹配的删除key操作
	 * @param key    key
	 * @param value  value 只有值相同才会成功删除
	 * @return
	 *   - true  删除成功
	 *   - false 删除失败 key不存在/key-value不匹配/key-value匹配后刚好失效
	 */
	boolean remove(String key, String value);
	
	/**
	 * CAS，成功返回true，失败返回false。
	 * 注意：在高并发场景下，过多线程使用该方法将导致过多无用的重试，从而大幅降低性能。
	 * @param value 不支持设置为null，请使用remove(key, oldValue)
	 * @param expireSeconds 超时时间，如果是null，则等于不改变，原来是多少秒就多少秒
	 */
	boolean compareAndSet(String key, String value, String oldValue, Integer expireSeconds);
	
	///////////////////// RedisLimit 限制次数 ///////////////////////
	
	/**
	 * 查询key的redis限制剩余次数。
	 * @param limitParam 限制参数
	 * @param key 业务主键
	 * @return -1是系统异常，正常值大于等于0
	 */
	long getLimitCount(RedisLimitParam limitParam, String key);
	
	/**
	 * 判断是否还有限制次数。
	 * @param limitParam
	 * @param key
	 * @return
	 */
	boolean hasLimitCount(RedisLimitParam limitParam, String key);
	
	/**
	 * 使用了一次限制。一般来说，业务都是在处理成功后才扣减使用是否成功的限制，
	 * 如果使用失败了，如果业务支持事务回滚，那么可以回滚掉，此时可以不用RedisTransation做全局限制。
	 * 
	 * @param limitEnum
	 * @param key
	 * @return 返回是当前周期内第几个使用配额的，如果返回-1，表示使用配额失败
	 */
	long useLimitCount(RedisLimitParam limitEnum, String key);
	
	/**
	 * 使用了count次限制。一般来说，业务都是在处理成功后才扣减使用是否成功的限制，
	 * 如果使用失败了，如果业务支持事务回滚，那么可以回滚掉，此时可以不用RedisTransation做全局限制。
	 * 
	 * @param limitParam
	 * @param key
	 * @param count 一次可以使用掉多个count
	 * @return 返回是当前周期内第几个使用配额的，如果返回-1，表示使用配额失败
	 */
	long useLimitCount(RedisLimitParam limitParam, String key, int count);
	
	/////////////////// Redis Lock 分布式锁 ////////////////////////
	
	/**
	 * 获得一个名称为key的锁，redis保证同一时刻只有一个client可以获得锁。
	 * 
	 * @param namespace 命名空间，每个应用独立的空间
	 * @param key 业务key，redis将保证同一个namespace同一个key只有一个client可以拿到锁
	 * @param maxTransactionSeconds 单位秒，必须大于0,拿到锁之后,预计多久可以完成这个事务，如果超过这个时间还没有归还锁，那么事务将失败
	 * @return 如果加锁成功，返回锁的唯一识别字符，可用于解锁；如果加锁失败，则返回null
	 */
	String requireLock(String namespace, String key, int maxTransactionSeconds);

	/**
	 * 续期锁的有效期
	 *
	 * @param namespace 命名空间，每个应用独立的空间
	 * @param key 业务key，redis将保证同一个namespace同一个key只有一个client可以拿到锁
	 * @param maxTransactionSeconds 单位秒，必须大于0，锁的有效期
	 * @param lockUuid 锁的uuid，提供对的uuid才进行续期
	 * @return 续期成功返回true，否则返回false
	 */
	boolean renewalLock(String namespace, String key, String lockUuid, int maxTransactionSeconds);

	/**
	 * 如果事务已经完成，则归还锁。
	 * @param namespace
	 * @param key
	 */
	boolean releaseLock(String namespace, String key, String lockUuid);
	
	/////////////////// Redis Auto Increment ID 分布式自增id //////////////////////
	
	/**
	 * 获得自增id，从1开始
	 * @param namespace 必须，由使用方自定决定，用于区分不同的业务。实际redis key会加上_ID后缀
	 * @return 没有重试，获取失败返回null，注意判断和重试
	 */
	Long getAutoIncrementId(String namespace);

	/////////////////// Redis 带 ACK 机制的消息队列 ///////////////////////////////

	/**
	 * 发送消息，返回消息的uuid。默认的超时时间是30秒
	 * @param topic topic将是redis的key
	 * @param msg
	 * @return
	 */
	String send(String topic, String msg);

	/**
	 * 发送消息，返回消息的uuid
	 * @param topic 即redis的key
	 * @param msg
	 * @param defaultAckTimeoutSec 默认ack超时时间：当消费者消费了消息却没来得及设置ack超时时间时的默认超时秒数。建议处理时间默认比较长的应用，可以将该值设置较大，例如60秒或120秒
	 * @return 消息的uuid，发送失败返回null
	 */
	String send(String topic, String msg, int defaultAckTimeoutSec);
	
	/**
	 * 批量发送消息，返回消息的uuid。默认的超时时间是30秒
	 * @param topic topic将是redis的key
	 * @param msgList 消息列表
	 * @return 消息的uuidList，发送失败返回null
	 */
	List<String> sendBatch(String topic, List<String> msgList);
	
	/**
	 * 批量发送消息，返回消息的uuid
	 * @param topic 即redis的key
	 * @param msgList 消息列表
	 * @param defaultAckTimeoutSec 默认ack超时时间：当消费者消费了消息却没来得及设置ack超时时间时的默认超时秒数。
	 *                             建议处理时间默认比较长的应用，可以将该值设置较大，例如60秒或120秒
	 * @return 消息的uuidList，发送失败返回null
	 */
	List<String> sendBatch(String topic, List<String> msgList, int defaultAckTimeoutSec);
	
	/**
	 * 接收消息，永久阻塞式，使用默认的actTimeout值
	 * @param topic 即redis的key
	 * @return
	 */
	RedisMsg receive(String topic);

	/**
	 * 接收消息
	 * @param topic 即redis的key
	 * @param waitTimeoutSec 指定接口阻塞等待时间，0表示不阻塞，-1表示永久等待，大于0为等待的秒数
	 * @param ackTimeoutSec ack确认超时的秒数，设置为null则表示不修改，使用发送方设置的默认超时值
	 * @return 如果没有接收到消息，返回null
	 */
	RedisMsg receive(String topic, int waitTimeoutSec, Integer ackTimeoutSec);

	/**
	 * 确认消费消息成功，删除消息
	 * @param topic 即redis的key
	 * @param msgUuid
	 * @return
	 */
	boolean ack(String topic, String msgUuid);
	
	/**
	 * 确认消费消息失败，复原消息
	 *   将消息复原后，可立即被消费
	 *   而超时的清理复原消息，被消费的频率会被超时时间控制
	 * @param topic 即redis的key
	 * @param msgUuid
	 * @return
	 */
	boolean nack(String topic, String msgUuid);
	
	/**
	 * 清理整个topic数据
	 * @param topic 即redis的key
	 * @return
	 */
	boolean removeTopic(String topic);

	/**
	 * 消息队列状态
	 * @param topic
	 * @return
	 */
	RedisQueueStatus getQueueStatus(String topic);

}
