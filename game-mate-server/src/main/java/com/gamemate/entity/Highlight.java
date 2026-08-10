package com.gamemate.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("highlight")
public class Highlight {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long gameId;

    private String title;

    private String videoUrl;

    private String thumbnail;

    private Integer duration;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}