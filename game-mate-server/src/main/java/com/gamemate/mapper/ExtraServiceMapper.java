package com.gamemate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gamemate.entity.ExtraService;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ExtraServiceMapper extends BaseMapper<ExtraService> {

    @Select("SELECT * FROM extra_service ORDER BY create_time ASC")
    List<ExtraService> findAll();
}