package com.atguigu.gmall.index.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.aspect.GmallCache;
import com.atguigu.gmall.index.client.GmallPmsClient;
import com.atguigu.gmall.index.service.IndexService;
import com.atguigu.gmall.index.utils.DistributedLock;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RCountDownLatch;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author Lee
 * @date 2020-10-09  17:52
 */
@Service
public class IndexServiceImpl implements IndexService {

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private DistributedLock distributedLock;

    @Autowired
    private RedissonClient redissonClient;

    public static final String KEY_PREFIX = "index:category:";

    // AOP实现的方法
    @Override
    @GmallCache(prefix = KEY_PREFIX, timeout = 129600, random = 7200, lock = "lock:")
    public List<CategoryEntity> queryLevelOneCategories() {
        System.out.println("目标方法");
        ResponseVo<List<CategoryEntity>> responseVo = this.pmsClient.queryAllCategory(0L);
        List<CategoryEntity> categoryEntities = responseVo.getData();
        return categoryEntities;
    }

    // 原方法
    /*public List<CategoryEntity> queryLevelOneCategories2() {
        // 1、查询缓存中是否存在，若存在，直接从缓存中命中
        String json = redisTemplate.opsForValue().get(KEY_PREFIX + "0");
        if (StringUtils.isNotBlank(json)) {
            // 如果缓存中有，直接返回
            return JSON.parseArray(json, CategoryEntity.class);
        }

        // 2、若缓存中不存在，远程调用从MySQL中查询出所需数据并存入redis中
        ResponseVo<List<CategoryEntity>> responseVo = this.pmsClient.queryAllCategory(0L);
        List<CategoryEntity> categoryEntities = responseVo.getData();
        // 这里已经解决了缓存穿透问题（没有判空，即使空我们也存入进去了）
        // 下方为解决缓存雪崩
        if (CollectionUtils.isEmpty(categoryEntities)) {
            this.redisTemplate.opsForValue().set(KEY_PREFIX + "0", JSON.toJSONString(categoryEntities), 5, TimeUnit.MINUTES);
        } else {
            this.redisTemplate.opsForValue().set(KEY_PREFIX + "0", JSON.toJSONString(categoryEntities), 90, TimeUnit.DAYS);
        }
        return categoryEntities;
    }*/

    // AOP实现的方法
    @Override
    @GmallCache(prefix = KEY_PREFIX, timeout = 129600, random = 7200, lock = "lock:")
    public List<CategoryEntity> queryLevelTwoWithSubByPid(Long parentId) {

        System.out.println("目标方法");
        ResponseVo<List<CategoryEntity>> responseVo = this.pmsClient.queryCategoriesLevelTwoWithSubByParentId(parentId);
        List<CategoryEntity> categoryEntities = responseVo.getData();

        return categoryEntities;
    }

    // 原方法
    /*public List<CategoryEntity> queryLevelTwoWithSubByPid2(Long parentId) {

        // 1、查询缓存中是否存在，若存在，直接从缓存中命中
        String json = redisTemplate.opsForValue().get(KEY_PREFIX + parentId);
        if (StringUtils.isNotBlank(json)) {
            // 如果缓存中有，直接返回
            return JSON.parseArray(json, CategoryEntity.class);
        }

        // 2、若缓存中不存在，远程调用从MySQL中查询出所需数据并存入redis中
        ResponseVo<List<CategoryEntity>> responseVo = this.pmsClient.queryCategoriesLevelTwoWithSubByParentId(parentId);
        List<CategoryEntity> categoryEntities = responseVo.getData();
        // 这里已经解决了缓存穿透问题（没有判空，即使空我们也存入进去了）
        // 下方为解决缓存雪崩
        if (CollectionUtils.isEmpty(categoryEntities)) {
            this.redisTemplate.opsForValue().set(KEY_PREFIX + parentId, JSON.toJSONString(categoryEntities), 5, TimeUnit.MINUTES);
        } else {
            this.redisTemplate.opsForValue().set(KEY_PREFIX + parentId, JSON.toJSONString(categoryEntities), 90, TimeUnit.DAYS);
        }
        return categoryEntities;
    }*/

    // 测试本地锁和分布式锁的方法
    @Override
    public void testLock() {
        // 分布式锁框架Redisson
        RLock lock = this.redissonClient.getLock("lock"); // 只要锁的名称相同就是同一把锁
        lock.lock(9, TimeUnit.SECONDS); // 加锁

        try {
            // 查询redis中的num值
            String numString = this.redisTemplate.opsForValue().get("num");
            // 没有该值就设置为1
            if (StringUtils.isBlank(numString)) {
                this.redisTemplate.opsForValue().set("num", "1");
            }
            // 有值就转换为int
            int num = Integer.parseInt(numString);
            // 把redis中的num值+1
            this.redisTemplate.opsForValue().set("num", String.valueOf(++num));
        } finally {
//            lock.unlock(); // 解锁
        }


    }

    /*public void testLock2() throws InterruptedException {
        String uuid = UUID.randomUUID().toString();
        // 获取锁
        Boolean lock = this.distributedLock.tryLock("lock", uuid, 9L);
        if (lock) { // 如果获取锁成功再执行业务逻辑
            String numString = this.redisTemplate.opsForValue().get("num");
            // 没有该值就设置为1
            if (StringUtils.isBlank(numString)) {
                this.redisTemplate.opsForValue().set("num", "1");
            }
            int num = Integer.parseInt(numString);
            this.redisTemplate.opsForValue().set("num", String.valueOf(++num));

//            TimeUnit.SECONDS.sleep(60);

            // 测试可重入锁
            this.testSubLock(uuid);

            // 执行完业务逻辑之后释放锁
            this.distributedLock.unlock("lock", uuid);
        }

    }*/

    /*public void testSubLock(String uuid) {
        // 加锁
        Boolean lock = this.distributedLock.tryLock("lock", uuid, 9L);
        if (lock) {
            System.out.println("测试分布式锁的可重入。。。。。。");
            // 解锁
            this.distributedLock.unlock("lock", uuid);
        }
    }*/

    @Override
    // 测试Redisson读锁的方法
    public String readLock() {
        // 初始化读写锁
        RReadWriteLock readwriteLock = redissonClient.getReadWriteLock("readwriteLock");
        RLock rLock = readwriteLock.readLock(); // 获取读锁

        rLock.lock(10, TimeUnit.SECONDS); // 加10s锁

        String msg = this.redisTemplate.opsForValue().get("msg");

        //rLock.unlock(); // 解锁

        return msg;
    }

    @Override
    // 测试Redisson写锁的方法
    public String writeLock() {
        // 初始化读写锁
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("readwriteLock");
        RLock rLock = readWriteLock.writeLock(); // 获取写锁

        rLock.lock(10, TimeUnit.SECONDS); // 加10s锁

        this.redisTemplate.opsForValue().set("msg", UUID.randomUUID().toString());

        //rLock.unlock(); // 解锁

        return "成功写入了内容。。。。。。";
    }

    @Override
    // 测试Redisson信号量的latch方法
    public String latch() {
        RCountDownLatch countDownLatch = this.redissonClient.getCountDownLatch("countdown");
        try {
            countDownLatch.trySetCount(6); // 6个学生在班里
            countDownLatch.await();// 都在等待，一次出去一个人，全都出去之后才会关门

            return "关门了。。。";
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    // 测试Redisson信号量的countDown方法
    public String countDown() {
        RCountDownLatch countDownLatch = this.redissonClient.getCountDownLatch("countdown");
        countDownLatch.countDown();// 一次出去一个人
        return "出来了一个人。。。";
    }

    /*public void testLock1() throws InterruptedException {
        // 1. 从redis中获取锁,setnx
        String uuid = UUID.randomUUID().toString();
        Boolean lock = this.redisTemplate.opsForValue().setIfAbsent("lock", uuid, 3, TimeUnit.SECONDS);
        if (lock) { // 拿到锁，执行业务逻辑
            // 查询redis中的num值
            String numString = this.redisTemplate.opsForValue().get("num");
            // 没有该值就设置为1
            if (StringUtils.isBlank(numString)) {
                this.redisTemplate.opsForValue().set("num", "1");
            }
            // 有值就转换为int
            int num = Integer.parseInt(numString);
            // 把redis中的num值+1
            this.redisTemplate.opsForValue().set("num", String.valueOf(++num));

            // 2. 业务逻辑执行完成之后，释放锁
            // 判断是否是自己的锁，如果是自己的锁才能释放
            //if (StringUtils.equals(uuid, this.redisTemplate.opsForValue().get("lock"))) {
                // 判断完成之后，过期时间刚好到期，导致该锁自动释放。此时再去执行delete会导致误删
            //    this.redisTemplate.delete("lock");
            // }
            // 使用lua脚本保证删除的原子性
            String script = "if redis.call('get',KEYS[1]) == ARGV[1] " +
                    "then " +
                    "return redis.call('del',KEYS[1]) " +
                    "else return 0 " +
                    "end";
            this.redisTemplate.execute(new DefaultRedisScript<>(script, Boolean.class), Arrays.asList("lock"), uuid);
        } else { // 没拿到锁
            // 3. 每隔1秒回调一次，再次尝试获取锁
            Thread.sleep(1000);
            this.testLock(); // 递归调用
        }

    }*/
}

