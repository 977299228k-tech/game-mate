package com.gamemate.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_settings")
public class UserSettings {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField(value = "user_id", insertStrategy = FieldStrategy.IGNORED)
    private Long userId;

    private String personality;

    private String voice;

    private Integer memoryEnabled;

    private Integer emotionEnabled;

    private Integer tacticEnabled;

    private Integer guideEnabled;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime updateTime;
}