package com.gamemate.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("order_extra")
public class OrderExtra {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private Long extraId;
}