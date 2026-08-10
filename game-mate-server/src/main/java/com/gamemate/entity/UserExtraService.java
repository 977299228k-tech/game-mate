package com.gamemate.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("user_extra_service")
public class UserExtraService {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long extraId;

    private Integer totalHours;

    private Integer usedHours;

    private BigDecimal paidPrice;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
