package com.gamemate.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class GameVO {

    private Long id;

    private String name;

    private String genre;

    private String icon;

    private String imageUrl;

    private String color;

    private String description;

    private String tags;

    private Integer isCustom;

    private LocalDateTime createTime;
}