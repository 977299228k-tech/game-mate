package com.gamemate.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderVO {

    private Long id;

    private Long userId;

    private String planName;

    private BigDecimal totalPrice;

    private Integer hours;

    private String status;

    private String payMethod;

    private List<ExtraServiceVO> extraServices;

    private LocalDateTime createTime;
}