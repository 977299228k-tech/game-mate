package com.gamemate.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class HighlightVO {

    private Long id;

    private Long userId;

    private Long gameId;

    private String gameName;

    private String title;

    private String videoUrl;

    private String thumbnail;

    private Integer duration;

    private LocalDateTime createTime;
}