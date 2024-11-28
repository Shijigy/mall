package com.seckill.mall.dao;

import com.seckill.mall.entity.Order;
import org.apache.ibatis.annotations.Insert;

/**
 * @Author 0011
 * @Description: 操作数据库的order表
 */
public interface OrderMapper {
    /**
     * 生成订单
     *
     * @param order 订单信息
     * @return
     */
    @Insert("insert into `order` (pid,uid) values (#{pid},#{uid})")
    int insertOrder(Order order);
}


