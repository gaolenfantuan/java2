package com.example.springboottest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottest.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper
 * 继承 BaseMapper 后，基础的增删改查方法都自动具备（不需要写 SQL）：
 * insert / deleteById / updateById / selectById / selectList / selectOne ...
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
