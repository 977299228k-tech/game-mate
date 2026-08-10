package com.gamemate.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserVO {

    private Long id;

    private String account;

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

    private String token;

    private LocalDateTime createTime;
}