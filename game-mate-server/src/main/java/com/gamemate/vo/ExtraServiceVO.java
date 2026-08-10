package com.gamemate.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ExtraServiceVO {

    private Long id;

    private String name;

    private String description;

    private String icon;

    private String color;

    private BigDecimal price;
}