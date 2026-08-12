package com.gamemate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gamemate.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    @Select("SELECT * FROM `order` WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Order> findByUserId(Long userId);

    @Update("UPDATE `order` SET status = 'PAID', pay_method = COALESCE(#{payMethod}, pay_method) "
            + "WHERE id = #{orderId} AND user_id = #{userId} AND status = 'UNPAID'")
    int markPaidIfUnpaid(@Param("userId") Long userId,
                         @Param("orderId") Long orderId,
                         @Param("payMethod") String payMethod);
}
