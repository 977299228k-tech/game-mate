package com.gamemate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gamemate.entity.Plan;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PlanMapper extends BaseMapper<Plan> {

    @Select("SELECT * FROM plan ORDER BY is_popular DESC, create_time ASC")
    List<Plan> findAll();
}