package com.example.springboottest.controller;

import com.example.springboottest.common.Result;
import com.example.springboottest.service.HomeService;
import com.example.springboottest.vo.CategoryVO;
import com.example.springboottest.vo.HomeItemVO;
import com.example.springboottest.vo.HomeStatsVO;
import com.example.springboottest.vo.PageVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 首页接口（搜索 + 内容展示 + 分类筛选）
 *
 * 前端（Vite 已把 /api 代理到 localhost:8080）调用：
 *   GET http://localhost:5173/api/home/categories
 *   GET http://localhost:5173/api/home/items?keyword=草莓&categoryId=1&sort=hot&page=1&size=12
 *   GET http://localhost:5173/api/home/hot?limit=6
 *   GET http://localhost:5173/api/home/items/1
 *   GET http://localhost:5173/api/home/stats
 */
@RestController
@RequestMapping("/api/home")
public class HomeController {

    private final HomeService homeService;

    public HomeController(HomeService homeService) {
        this.homeService = homeService;
    }

    /**
     * 分类树（一级分类 + 二级分类，带内容数量）
     * 前端用它渲染「全部 / 甜点 / 穿搭 / 好物 / 心情」以及点开一级分类后的二级筛选条
     */
    @GetMapping("/categories")
    public Result<List<CategoryVO>> categories() {
        return Result.success(homeService.getCategoryTree());
    }

    /**
     * 搜索 / 列表
     *
     * @param keyword  关键词，可空格分隔多个词，如「草莓 蛋糕」
     * @param categoryId 分类ID（一级分类会自动包含其下所有二级分类）
     * @param includeChildren 是否包含子分类，默认 true（传 false 只看该分类本身）
     * @param sort     default 默认排序 / hot 按点赞 / new 按最新
     * @param page     页码，从 1 开始
     * @param size     每页条数，最大 50
     */
    @GetMapping("/items")
    public Result<PageVO<HomeItemVO>> items(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean includeChildren,
            @RequestParam(required = false, defaultValue = "default") String sort,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "12") Integer size) {

        return Result.success(homeService.search(keyword, categoryId, includeChildren, sort, page, size));
    }

    /**
     * 热门推荐（按点赞数），首页横幅下方想加「大家都在看」时可以直接用
     */
    @GetMapping("/hot")
    public Result<List<HomeItemVO>> hot(@RequestParam(required = false, defaultValue = "6") Integer limit) {
        int size = (limit == null || limit < 1) ? 6 : Math.min(limit, 20);
        return Result.success(homeService.search(null, null, true, "hot", 1, size).getList());
    }

    /**
     * 内容详情（每次访问浏览量 +1）
     */
    @GetMapping("/items/{id}")
    public Result<HomeItemVO> detail(@PathVariable Long id) {
        return Result.success(homeService.getDetail(id));
    }

    /**
     * 首页横幅统计：内容总数 / 分类总数 / 累计点赞 / 热门数
     */
    @GetMapping("/stats")
    public Result<HomeStatsVO> stats() {
        return Result.success(homeService.getStats());
    }
}
