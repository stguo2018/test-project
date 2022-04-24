package com.ews.stguo.testproject.controllers;

import com.ews.stguo.testproject.distributedlock.redis.RedisLockTool;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.Jedis;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
@RestController
@Slf4j
public class RedisDistributedLockController {

    private static final String HOST = "localhost";

    @Scheduled(cron = "0/5 * * * * ?")
    public void test() throws Exception {
        System.out.println(LocalDateTime.now().toString());
        Thread.sleep(30000);
    }

    /**
     * @PostConstruct
     */
    public void init() {
        String uuid = UUID.randomUUID().toString();
         String lockKey = "test";
        ThreadFactory tf = new ThreadFactoryBuilder().setNameFormat("redis-distributed-lock-pool-%d").build();
        ExecutorService executorService = executorService = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(1024), tf);
        executorService.execute(() -> {
            try (Jedis jedis = new Jedis(HOST)) {
                log.info("{} start...", uuid);
                while (true) {
                    if (RedisLockTool.checkLockExists(jedis, lockKey) && !RedisLockTool.checkLockIsHold(jedis, lockKey, uuid)) {
                        Thread.sleep(5000);
                        continue;
                    } else if (RedisLockTool.checkLockExists(jedis, lockKey) && RedisLockTool.checkLockIsHold(jedis, lockKey, uuid) &&
                        RedisLockTool.tryGetDistributedLockWithUpdate(jedis, lockKey, uuid, 30000, 60000, 1000)) {
                        log.info("{} hold lock update success.", uuid);
                        Thread.sleep(10000);
                        continue;
                    }
                    if (RedisLockTool.tryGetDistributedLock(jedis, lockKey, uuid, 30000, 60000, 1000)) {
                        log.info("{} hold lock success.", uuid);
                    } else {
                        log.warn("{} hold lock failed.", uuid);
                    }
                    log.info("{} started...", uuid);
                    Thread.sleep(10000);
                }
            } catch (Exception e) {
                log.error("{} hold lock failed.", uuid, e);
            }
        });
    }

    @RequestMapping(path = "/redis/lock/get", method = RequestMethod.GET)
    public String getRedisLock(@RequestParam("requestId") String reuestId,
                             @RequestParam("lockKey") String lockKey,
                             @RequestParam("expireTime") Integer expireTime) throws InterruptedException {
        log.info("{}...", reuestId);
        Jedis jedis = new Jedis(HOST);
        //noinspection AlibabaUndefineMagicConstant
        if (RedisLockTool.tryGetDistributedLock(jedis, lockKey, reuestId, expireTime, 30000, 1000)) {
            log.info("{} get lock success.", reuestId);
            Thread.sleep(15000);
            if (RedisLockTool.releaseDistributedLock(jedis, lockKey, reuestId)) {
                log.info("{} release lock success.", reuestId);
            }
        }
        return "success";
    }

    @RequestMapping(path = "/redis/lock/release", method = RequestMethod.GET)
    public String getRedisLock(@RequestParam("requestId") String reuestId, @RequestParam("lockKey") String lockKey) {
        log.info("{}...", reuestId);
        Jedis jedis = new Jedis(HOST);
        if (RedisLockTool.releaseDistributedLock(jedis, lockKey, reuestId)) {
            log.info("{} release lock success.", reuestId);
            return "success";
        }
        return "failed";
    }

}
