package com.gamemate.controller;

import com.gamemate.common.Result;
import com.gamemate.dto.OrderCreateDTO;
import com.gamemate.service.OrderService;
import com.gamemate.vo.OrderVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public Result<OrderVO> createOrder(
            HttpServletRequest request,
            @Valid @RequestBody OrderCreateDTO dto) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(orderService.createOrder(userId, dto));
    }

    @PostMapping("/{orderId}/pay")
    public Result<OrderVO> payOrder(
            HttpServletRequest request,
            @PathVariable Long orderId,
            @RequestParam(required = false) String payMethod) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(orderService.payOrder(userId, orderId, payMethod));
    }

    @GetMapping("/list")
    public Result<List<OrderVO>> getOrderList(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(orderService.getOrderList(userId));
    }

    @GetMapping("/{orderId}")
    public Result<OrderVO> getOrderDetail(
            HttpServletRequest request,
            @PathVariable Long orderId) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(orderService.getOrderDetail(userId, orderId));
    }
}
