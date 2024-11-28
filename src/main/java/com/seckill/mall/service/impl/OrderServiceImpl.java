package com.seckill.mall.service.impl;

import com.seckill.mall.dao.OrderMapper;
import com.seckill.mall.entity.Order;
import com.seckill.mall.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceImpl implements OrderService {

    private OrderMapper orderMapper;

    @Override
    public int insert(Order order) {
        return orderMapper.insertOrder(order);
    }
}

