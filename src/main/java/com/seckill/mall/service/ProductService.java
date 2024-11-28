package com.seckill.mall.service;

import com.seckill.mall.entity.Product;

public interface ProductService {

    Product selectById(long id);

    int seckill(long id);
}
