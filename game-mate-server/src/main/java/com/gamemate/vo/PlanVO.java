package com.gamemate.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PlanVO {

    private Long id;

    private String name;

    private Integer hours;

    private BigDecimal price;

    private BigDecimal originalPrice;

    private Integer isPopular;

    private List<ExtraServiceVO> extraServices;

    private LocalDateTime createTime;
}