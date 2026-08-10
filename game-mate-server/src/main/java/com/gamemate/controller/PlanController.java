package com.gamemate.controller;

import com.gamemate.common.Result;
import com.gamemate.service.PlanService;
import com.gamemate.vo.ExtraServiceVO;
import com.gamemate.vo.PlanVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plan")
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;

    @GetMapping("/list")
    public Result<List<PlanVO>> getPlanList() {
        return Result.success(planService.getPlanList());
    }

    @GetMapping("/extra/list")
    public Result<List<ExtraServiceVO>> getExtraServiceList() {
        return Result.success(planService.getExtraServiceList());
    }

    @GetMapping("/extra/{id}")
    public Result<ExtraServiceVO> getExtraService(@PathVariable Long id) {
        return Result.success(planService.getExtraService(id));
    }
}