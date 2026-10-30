package com.example.springboottest.service;

import com.example.springboottest.vo.CategoryVO;
import com.example.springboottest.vo.HomeItemVO;
import com.example.springboottest.vo.HomeStatsVO;
import com.example.springboottest.vo.PageVO;

import java.util.List;

/**
 * 首页业务接口
 */
public interface HomeService {

    /**
     * 分类树：一级分类 + 它们下面的二级分类，并带上每个分类的内容数量
     */
    List<CategoryVO> getCategoryTree();

    /**
     * 搜索 / 列表
     *
     * @param keyword         关键词，可以空格分隔多个词（每个词都要命中）
     * @param categoryId      分类ID；传一级分类时会自动包含它的所有二级分类
     * @param includeChildren 是否包含子分类，默认 true
     * @param sort            排序：default / hot / new
     * @param page            页码，从 1 开始
     * @param size            每页条数
     */
    PageVO<HomeItemVO> search(String keyword, Long categoryId, Boolean includeChildren,
                              String sort, Integer page, Integer size);

    /**
     * 内容详情（同时把浏览量 +1）
     */
    HomeItemVO getDetail(Long id);

    /**
     * 首页横幅统计
     */
    HomeStatsVO getStats();
}
