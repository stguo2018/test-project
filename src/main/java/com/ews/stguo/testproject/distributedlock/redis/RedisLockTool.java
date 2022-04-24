package com.ews.stguo.testproject.distributedlock.redis;

import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

import java.util.Collections;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
@Slf4j
public class RedisLockTool {

    private static final String LOCK_SUCCESS = "OK";
    private static final Long SUCCESS = 1L;
    // NX - 表示只有key不存在时才进行插入， XX - 表示只有key存在是才进行插入
    private static final String SET_IF_NOT_EXIST = "NX";
    private static final String SET_IF_EXIST = "XX";
    // PX代表毫秒， EX代表秒
    private static final String SET_WITH_EXPIRE_TIME = "PX";

    private RedisLockTool() {}

    /**
     * 获取分布式锁
     * @param jedis redis客户端对象
     * @param lockKey 锁键
     * @param requestId 标识
     * @param expireTime 锁过期时间
     * @return 是否获取成功
     */
    public static boolean tryGetDistributedLock(Jedis jedis, String lockKey, String requestId, long expireTime, long timeout, long interval) throws InterruptedException {
        return getLock(jedis, lockKey, requestId, expireTime, timeout, interval, SET_IF_NOT_EXIST);
    }

    public static boolean tryGetDistributedLockWithUpdate(Jedis jedis, String lockKey, String requestId, long expireTime, long timeout, long interval) throws InterruptedException {
        return getLock(jedis, lockKey, requestId, expireTime, timeout, interval, SET_IF_EXIST);
    }

    private static boolean getLock(Jedis jedis, String lockKey, String requestId, long expireTime, long timeout, long interval, String nxxx) throws InterruptedException {
        long endTime = System.currentTimeMillis() + timeout;
        while (System.currentTimeMillis() < endTime || timeout == -1) {
            String result = jedis.set(lockKey, requestId, nxxx, SET_WITH_EXPIRE_TIME, expireTime);
            if (LOCK_SUCCESS.equals(result)) {
                return true;
            }
            Thread.sleep(interval);
        }
        return false;
    }

    public static boolean tryGetDistributedLockWithTimeout(Jedis jedis, String lockKey, String requestId, long expireTime, long timeout) throws InterruptedException {
        return tryGetDistributedLock(jedis, lockKey, requestId, expireTime, timeout, 10000);
    }

    public static boolean tryGetDistributedLockWithInterval(Jedis jedis, String lockKey, String requestId, long expireTime, long interval) throws InterruptedException {
        return tryGetDistributedLock(jedis, lockKey, requestId, expireTime, -1, interval);
    }

    public static boolean tryGetDistributedLock(Jedis jedis, String lockKey, String requestId, long expireTime) throws InterruptedException {
        return tryGetDistributedLock(jedis, lockKey, requestId, expireTime, -1, 10000);
    }

    /**
     * 释放分布式锁
     * @param jedis redis客户端对象
     * @param lockKey 锁键
     * @param requestId 标识
     * @return 是否释放成功
     */
    public static boolean releaseDistributedLock(Jedis jedis, String lockKey, String requestId) {
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Object result = jedis.eval(script, Collections.singletonList(lockKey), Collections.singletonList(requestId));
        return SUCCESS.equals(result);
    }

    public static boolean checkLockIsHold(Jedis jedis, String lockKey, String requestId) {
        return requestId.equals(jedis.get(lockKey));
    }

    public static boolean checkLockExists(Jedis jedis, String lockKey) {
        return jedis.exists(lockKey);
    }

}
