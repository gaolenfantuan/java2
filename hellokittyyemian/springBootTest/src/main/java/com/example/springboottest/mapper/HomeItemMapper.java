package com.example.springboottest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottest.entity.HomeItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 首页内容 Mapper（继承 BaseMapper，增删改查自动具备）
 */
@Mapper
public interface HomeItemMapper extends BaseMapper<HomeItem> {
}
