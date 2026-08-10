package com.gamemate.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

@Data
public class GameCreateDTO {

    @NotBlank(message = "游戏名称不能为空")
    private String name;

    private String genre;

    private String icon;

    private String color;

    private String description;

    private String imageUrl;

    private List<String> tags;
}