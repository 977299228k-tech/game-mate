package com.gamemate.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("game")
public class Game {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String genre;

    private String icon;

    private String imageUrl;

    private String color;

    private String description;

    private String tags;

    private Integer isCustom;

    private Long userId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}