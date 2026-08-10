package com.gamemate.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("order")
public class Order {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long planId;

    private BigDecimal totalPrice;

    private Integer hours;

    private String status;

    private String payMethod;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}