package com.gamemate.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String account;

    private String password;

    private String nickname;

    private String phone;

    private String email;

    private String avatar;

    private Integer balance;

    private String personality;

    private String voice;

    private Integer memoryEnabled;

    private Integer emotionEnabled;

    private Integer tacticEnabled;

    private Integer guideEnabled;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}