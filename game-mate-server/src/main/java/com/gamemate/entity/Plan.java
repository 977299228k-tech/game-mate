package com.gamemate.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("plan")
public class Plan {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private Integer hours;

    private java.math.BigDecimal price;

    private java.math.BigDecimal originalPrice;

    private Integer isPopular;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}