package com.gamemate.service.impl;

import com.gamemate.entity.ExtraService;
import com.gamemate.entity.Plan;
import com.gamemate.mapper.ExtraServiceMapper;
import com.gamemate.mapper.PlanMapper;
import com.gamemate.service.PlanService;
import com.gamemate.vo.ExtraServiceVO;
import com.gamemate.vo.PlanVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlanServiceImpl implements PlanService {

    private final PlanMapper planMapper;
    private final ExtraServiceMapper extraServiceMapper;

    @Override
    public List<PlanVO> getPlanList() {
        List<Plan> plans = planMapper.findAll();
        return plans.stream().map(this::convertToPlanVO).collect(Collectors.toList());
    }

    @Override
    public List<ExtraServiceVO> getExtraServiceList() {
        List<ExtraService> services = extraServiceMapper.findAll();
        return services.stream().map(this::convertToExtraServiceVO).collect(Collectors.toList());
    }

    @Override
    public ExtraServiceVO getExtraService(Long id) {
        ExtraService service = extraServiceMapper.selectById(id);
        if (service == null) {
            throw new RuntimeException("增值服务不存在");
        }
        return convertToExtraServiceVO(service);
    }

    private PlanVO convertToPlanVO(Plan plan) {
        PlanVO vo = new PlanVO();
        vo.setId(plan.getId());
        vo.setName(plan.getName());
        vo.setHours(plan.getHours());
        vo.setPrice(plan.getPrice());
        vo.setOriginalPrice(plan.getOriginalPrice());
        vo.setIsPopular(plan.getIsPopular());
        vo.setCreateTime(plan.getCreateTime());
        return vo;
    }

    private ExtraServiceVO convertToExtraServiceVO(ExtraService service) {
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