package com.gamemate.service;

import com.gamemate.vo.ExtraServiceVO;
import com.gamemate.vo.PlanVO;

import java.util.List;

public interface PlanService {

    List<PlanVO> getPlanList();

    List<ExtraServiceVO> getExtraServiceList();

    ExtraServiceVO getExtraService(Long id);
}