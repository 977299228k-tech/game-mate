package com.gamemate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChatMessageDTO {

    @NotNull(message = "游戏ID不能为空")
    private Long gameId;

    private String role;

    @NotBlank(message = "消息内容不能为空")
    private String content;
}