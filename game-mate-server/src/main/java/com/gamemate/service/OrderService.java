package com.gamemate.service;

import com.gamemate.dto.OrderCreateDTO;
import com.gamemate.vo.OrderVO;

import java.util.List;

public interface OrderService {

    OrderVO createOrder(Long userId, OrderCreateDTO dto);

    OrderVO payOrder(Long userId, Long orderId, String payMethod);

    List<OrderVO> getOrderList(Long userId);

    OrderVO getOrderDetail(Long userId, Long orderId);
}
