package com.gamemate.service.impl;

import com.gamemate.dto.OrderCreateDTO;
import com.gamemate.entity.ExtraService;
import com.gamemate.entity.Order;
import com.gamemate.entity.Plan;
import com.gamemate.entity.OrderExtra;
import com.gamemate.mapper.ExtraServiceMapper;
import com.gamemate.mapper.OrderMapper;
import com.gamemate.mapper.OrderExtraMapper;
import com.gamemate.mapper.PlanMapper;
import com.gamemate.service.OrderService;
import com.gamemate.service.UserService;
import com.gamemate.service.UserDataService;
import com.gamemate.vo.ExtraServiceVO;
import com.gamemate.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final PlanMapper planMapper;
    private final ExtraServiceMapper extraServiceMapper;
    private final OrderExtraMapper orderExtraMapper;
    private final UserService userService;
    private final UserDataService userDataService;

    @Override
    @Transactional
    public OrderVO createOrder(Long userId, OrderCreateDTO dto) {
        Plan plan = planMapper.selectById(dto.getPlanId());
        if (plan == null) {
            throw new RuntimeException("套餐不存在");
        }

        BigDecimal totalPrice = plan.getPrice();
        List<ExtraService> extraServices = new ArrayList<>();

        if (dto.getExtraIds() != null && !dto.getExtraIds().isEmpty()) {
            for (Long extraId : dto.getExtraIds()) {
                ExtraService extra = extraServiceMapper.selectById(extraId);
                if (extra != null) {
                    extraServices.add(extra);
                    totalPrice = totalPrice.add(extra.getPrice());
                }
            }
        }

        Order order = new Order();
        order.setUserId(userId);
        order.setPlanId(plan.getId());
        order.setTotalPrice(totalPrice);
        order.setHours(plan.getHours());
        order.setStatus("UNPAID");
        order.setPayMethod(dto.getPayMethod() != null ? dto.getPayMethod() : "wechat");
        orderMapper.insert(order);

        for (ExtraService extra : extraServices) {
            OrderExtra orderExtra = new OrderExtra();
            orderExtra.setOrderId(order.getId());
            orderExtra.setExtraId(extra.getId());
            orderExtraMapper.insert(orderExtra);
        }

        return convertToVO(order, plan, extraServices);
    }

    @Override
    @Transactional
    public OrderVO payOrder(Long userId, Long orderId, String payMethod) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此订单");
        }
        if (orderMapper.markPaidIfUnpaid(userId, orderId, payMethod) != 1) {
            throw new RuntimeException("订单已支付或状态已变更，请刷新后重试");
        }

        userService.addBalance(userId, order.getHours());

        List<OrderExtra> orderExtras = orderExtraMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<OrderExtra>()
                        .eq("order_id", orderId));
        for (OrderExtra orderExtra : orderExtras) {
            ExtraService extra = extraServiceMapper.selectById(orderExtra.getExtraId());
            if (extra != null) {
                // 增值服务按本订单套餐时长开通，后续可由 extra_service.hours 覆盖。
                userDataService.purchaseExtraService(userId, extra.getId(), order.getHours(), extra.getPrice());
            }
        }

        Plan plan = planMapper.selectById(order.getPlanId());
        order.setStatus("PAID");
        if (payMethod != null) {
            order.setPayMethod(payMethod);
        }
        return convertToVO(order, plan, loadExtras(orderId));
    }

    @Override
    public List<OrderVO> getOrderList(Long userId) {
        List<Order> orders = orderMapper.findByUserId(userId);
        List<OrderVO> result = new ArrayList<>();
        for (Order order : orders) {
            Plan plan = planMapper.selectById(order.getPlanId());
            result.add(convertToVO(order, plan, new ArrayList<>()));
        }
        return result;
    }

    @Override
    public OrderVO getOrderDetail(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("无权查看此订单");
        }
        Plan plan = planMapper.selectById(order.getPlanId());
        return convertToVO(order, plan, loadExtras(orderId));
    }

    private List<ExtraService> loadExtras(Long orderId) {
        return orderExtraMapper.selectList(
                        new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<OrderExtra>()
                                .eq("order_id", orderId))
                .stream()
                .map(OrderExtra::getExtraId)
                .map(extraServiceMapper::selectById)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }

    private OrderVO convertToVO(Order order, Plan plan, List<ExtraService> extraServices) {
        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setUserId(order.getUserId());
        vo.setPlanName(plan != null ? plan.getName() : "未知套餐");
        vo.setTotalPrice(order.getTotalPrice());
        vo.setHours(order.getHours());
        vo.setStatus(order.getStatus());
        vo.setPayMethod(order.getPayMethod());
        vo.setCreateTime(order.getCreateTime());

        if (!extraServices.isEmpty()) {
            List<ExtraServiceVO> extraVOs = extraServices.stream()
                    .map(this::convertExtraToVO)
                    .collect(Collectors.toList());
            vo.setExtraServices(extraVOs);
        }
        return vo;
    }

    private ExtraServiceVO convertExtraToVO(ExtraService service) {
        ExtraServiceVO vo = new ExtraServiceVO();
        vo.setId(service.getId());
        vo.setName(service.getName());
        vo.setDescription(service.getDescription());
        vo.setIcon(service.getIcon());
        vo.setColor(service.getColor());
        vo.setPrice(service.getPrice());
        return vo;
    }
}
