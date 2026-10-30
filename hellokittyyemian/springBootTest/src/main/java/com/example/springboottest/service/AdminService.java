package com.example.springboottest.service;

import com.example.springboottest.dto.AdminCategoryRequest;
import com.example.springboottest.dto.AdminItemRequest;
import com.example.springboottest.vo.AdminCategoryVO;
import com.example.springboottest.vo.AdminItemVO;
import com.example.springboottest.vo.AdminLoginVO;
import com.example.springboottest.vo.AdminStatsVO;
import com.example.springboottest.vo.PageVO;

import java.util.List;

/**
 * 后台管理业务
 *
 * 只负责「管理员登录 + 首页数据（内容 / 分类）的增删改查」，
 * 前台展示逻辑仍然在 HomeService 里，两边互不影响。
 */
public interface AdminService {

    // ---------- 登录 ----------

    /** 管理员登录，成功返回 token */
    AdminLoginVO login(String username, String password);

    /** 用 token 换取管理员信息（前端刷新页面时用来判断登录是否还有效） */
    AdminLoginVO current(String token);

    /** 退出登录，让 token 立刻失效 */
    void logout(String token);

    // ---------- 内容管理 ----------

    /** 内容列表（含已下架的，可按关键词 / 分类 / 状态筛选） */
    PageVO<AdminItemVO> searchItems(String keyword, Long categoryId, Integer status, Integer page, Integer size);

    /** 单条内容详情（编辑回填用） */
    AdminItemVO getItem(Long id);

    Long createItem(AdminItemRequest request);

    void updateItem(Long id, AdminItemRequest request);

    void deleteItem(Long id);

    // ---------- 分类管理 ----------

    /** 全部分类（平铺，带父分类名和内容数量） */
    List<AdminCategoryVO> listCategories();

    Long createCategory(AdminCategoryRequest request);

    void updateCategory(Long id, AdminCategoryRequest request);

    void deleteCategory(Long id);

    // ---------- 概览 ----------

    AdminStatsVO stats();
}
