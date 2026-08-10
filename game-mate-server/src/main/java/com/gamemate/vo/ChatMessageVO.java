package com.gamemate.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatMessageVO {

    private Long id;

    private Long userId;

    private Long gameId;

    private String role;

    private String content;

    private LocalDateTime createTime;
}