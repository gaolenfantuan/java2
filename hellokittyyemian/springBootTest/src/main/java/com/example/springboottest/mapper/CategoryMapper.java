package com.example.springboottest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottest.entity.Category;
import org.apache.ibatis.annotations.Mapper;

/**
 * 分类 Mapper（继承 BaseMapper，增删改查自动具备）
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}
