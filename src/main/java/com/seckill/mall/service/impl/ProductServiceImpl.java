package com.seckill.mall.service.impl;

import com.seckill.mall.dao.ProductMapper;
import com.seckill.mall.entity.Product;
import com.seckill.mall.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl implements ProductService {


    private ProductMapper productMapper;

    @Override
    public Product selectById(long id) {
        return productMapper.selectById(id);
    }

    @Override
    public int seckill(long id) {
        return productMapper.seckillById(id);
    }
}


