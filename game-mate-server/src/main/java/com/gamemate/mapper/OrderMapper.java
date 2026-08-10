package com.gamemate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gamemate.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    @Select("SELECT * FROM `order` WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Order> findByUserId(Long userId);
}