package org.example.tiktok.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.tiktok.dto.RedisData;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Component
@Slf4j
public class CacheClient {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedissonClient redissonClient;


    private final static ObjectMapper objectMapper = new ObjectMapper();

    public void set(String key, Object value, Long time, TimeUnit timeUnit) throws JsonProcessingException {
        String value2String = objectMapper.writeValueAsString(value);
        stringRedisTemplate.opsForValue().set(key,value2String,time,timeUnit);
    }

    public void setWithLogicalExpire(String key, Object value, Long time, TimeUnit timeUnit) throws JsonProcessingException {
        RedisData redisData = new RedisData();
        redisData.setData(value);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(timeUnit.toSeconds(time)));
        String value2String = objectMapper.writeValueAsString(redisData);

        stringRedisTemplate.opsForValue().set(key,value2String);
    }

    public <R,ID> R queryWithPassThrough(String keyPrefix, ID id, Class<R> type,
                                         Function<ID,R> dbCallback,Long time, TimeUnit timeUnit) throws JsonProcessingException {
        String key = keyPrefix + id;
        String json = stringRedisTemplate.opsForValue().get(key);
        if(StringUtils.isNotBlank(json)) {
            return objectMapper.readValue(json,type);
        }
        //空值
        if(json != null) {
            return null;
        }
        R data = dbCallback.apply(id);
        if(data == null) {
            this.set(key,"",time,timeUnit);
            return null;
        } else {
            this.set(key,data,time,timeUnit);
            return data;
        }
    }


     private static final ExecutorService CACHE_REBUILD_SERVICE = Executors.newFixedThreadPool(10);
    //R = result, ID = id
    public <R,ID> R queryWithLogicalExpire(String keyPrefix, ID id, Class<R> type,
                                           Function<ID,R> dbCallback, Long time, TimeUnit timeUnit) throws JsonProcessingException
    {
        String key = keyPrefix + id;
        String json = stringRedisTemplate.opsForValue().get(key);
        if(json == null ||json.isBlank()) {
            //hotRank会自动执行 不用担心没有数据
            return null;
        }
        //转化为redisData对象
        RedisData redisData = objectMapper.readValue(json, RedisData.class);
        //取出还未转化的json
        String dataJson = objectMapper.writeValueAsString(redisData.getData());
        //把Object的data转为需要type的对象
        R data =objectMapper.readValue(dataJson,type);
        LocalDateTime expireTime = redisData.getExpireTime();
        //数据未过期，返回数据
        if(expireTime.isAfter(LocalDateTime.now())) {
            return data;
        }
        RLock lock = redissonClient.getLock(key);
        //过期,重建
        if(lock.tryLock()) {
            try {
                CACHE_REBUILD_SERVICE.submit(() -> {
                    R result = dbCallback.apply(id);
                    try {
                        this.setWithLogicalExpire(key,result,time,timeUnit);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                });
            } catch (Exception e) {
                throw new RuntimeException();
            } finally {
                lock.unlock();
            }
        }
        log.warn("缓存命中，但数据已过期，返回旧数据，同时启动异步重建");
        return data;
    }
}
