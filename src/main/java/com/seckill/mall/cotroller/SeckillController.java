package com.seckill.mall.cotroller;

import com.seckill.mall.entity.Order;
import com.seckill.mall.service.OrderService;
import com.seckill.mall.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import cn.hutool.json.JSONUtil;


import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/seckill")
@Slf4j
public class SeckillController {

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ProductService productService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private static final String SECKILL_LOCK_PREFIX = "seckill_lock:";
    private static final String PRODUCT_STOCK_PREFIX = "product_stock:";

    /**
     * 秒杀请求处理，使用 Redis 分布式锁防止超卖，并通过 Kafka 异步写入订单
     *
     * @param pid 商品ID
     * @param uid 用户ID
     * @return 秒杀结果
     */
    @GetMapping("/{pid}/{uid}")
    public String seckill(@PathVariable long pid, @PathVariable long uid) {
        // 设置锁的键名
        String lockKey = SECKILL_LOCK_PREFIX + pid;
        String stockKey = PRODUCT_STOCK_PREFIX + pid;

        // 尝试获取分布式锁
        Boolean lockAcquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "locked", 10, TimeUnit.SECONDS);

        // 如果没有获取到锁，表示其他请求正在处理秒杀
        if (lockAcquired == null || !lockAcquired) {
            return "请稍后再试，秒杀人数太多了！";
        }

        try {
            // 检查商品库存
            String stock = redisTemplate.opsForValue().get(stockKey);
            if (stock == null || Integer.parseInt(stock) <= 0) {
                return "秒杀失败，商品已售罄！";
            }

            // 执行减库存操作
            long remainingStock = redisTemplate.opsForValue().decrement(stockKey);
            if (remainingStock < 0) {
                // 如果库存不足，恢复库存并返回失败
                redisTemplate.opsForValue().increment(stockKey);
                return "秒杀失败，商品已售罄！";
            }

            // 创建订单并通过 Kafka 异步处理
            Order order = new Order();
            order.setPid(pid);
            order.setUid(uid);
            // 将订单放入 Kafka 队列
            kafkaTemplate.send("seckill", JSONUtil.toJsonStr(order));
            return "秒杀成功，正在处理中...";

        } catch (Exception e) {
            log.error("秒杀处理出错: {}", e.getMessage(), e);
            return "秒杀失败，请稍后重试！";
        } finally {
            // 释放锁
            redisTemplate.delete(lockKey);
        }
    }

    // Kafka 消费者，异步处理订单
    @KafkaListener(topics = "seckill", groupId = "g1")
    public void consumer(ConsumerRecord<?, String> record) {
        String value = record.value();
        // 字符串转对象
        Order order = JSONUtil.toBean(value, Order.class);
        try {
            // 保存订单到数据库
            orderService.insert(order);
            log.info("订单处理成功，订单ID: {}", order.getId());
        } catch (Exception e) {
            log.error("处理订单失败: {}", e.getMessage(), e);
        }
    }
}


    /**
     * 数据预热到redis后的实现
     *
     * @param pid 商品id
     * @param uid 用户id
     * @return
     */
//    @GetMapping("/{pid}/{uid}")
//    public String seckillWithRedis(@PathVariable long pid, @PathVariable long uid) {
//        String end = redisTemplate.opsForValue().get("seckill:" + pid + ":end");
//        if (Objects.equals(end, "1")) {
//            return "秒杀结束";
//        }
//        //返回的decrement为剩余的库存
//        Long decrement = redisTemplate.opsForValue().decrement("seckill:" + pid + ":stock");
//        //剩余库存大于等于0都为秒杀成功
//        if (decrement >= 0) {
//            if (decrement == 0) {
//                //设置该商品秒杀状态为结束
//                redisTemplate.opsForValue().set("seckill:" + pid + ":end", "1");
//            }
//            //创建订单
//            Order order = new Order();
//            order.setPid(pid);
//            order.setUid(uid);
//            orderService.insert(order);
//        }
//        log.info("decrement:{}", decrement);
//        return decrement < 0 ? "秒杀失败" : "秒杀成功";
//    }

    /**
     * 未引入redis和kafka版实现
     *
     * @param pid 商品id
     * @param uid 用户id
     * @return
     */
//    @Transactional
//    @GetMapping("/{pid}/{uid}")
//    public synchronized String seckill(@PathVariable long pid, @PathVariable long uid) {
//        //1.从数据库获取商品的库存量
//        Product product = productService.selectById(pid);
//        int stock = product.getStock();
//        //2.判断库存是否足够,足够则进入秒杀
//        if (stock > 0) {
//            //执行秒杀逻辑
//            int result = productService.seckill(pid);
//            if (result == 1) {
//                //创建订单
//                Order order = new Order();
//                order.setPid(pid);
//                order.setUid(uid);
//                orderService.insert(order);
//            }
//            return result > 0 ? "秒杀成功" : "秒杀失败";
//        }
//        return "秒杀失败";
//    }


