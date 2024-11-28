package com.seckill.mall.dao;

import com.seckill.mall.entity.Product;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * @Author 0011
 * @Description: 操作数据库的product表
 */
public interface ProductMapper {
    /**
     * 根据id查询商品信息
     *
     * @param id 商品id
     * @return
     */
    @Select("select * from product where id = #{id}")
    Product selectById(long id);

    /**
     * 商品减库存
     *
     * @param id 商品id
     * @return
     */
    @Update("update product set stock = stock - 1 where id = #{id}")
    int seckillById(long id);
}
