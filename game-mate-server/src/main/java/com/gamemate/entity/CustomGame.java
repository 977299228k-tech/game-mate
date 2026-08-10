package com.gamemate.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("custom_game")
public class CustomGame {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String name;

    private String genre;

    private String icon;

    private String color;

    private String description;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}