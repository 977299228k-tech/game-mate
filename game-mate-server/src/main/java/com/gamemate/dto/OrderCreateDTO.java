package com.gamemate.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class OrderCreateDTO {

    @NotNull(message = "套餐ID不能为空")
    private Long planId;

    private List<Long> extraIds;

    @NotNull(message = "支付方式不能为空")
    private String payMethod;
}