package com.example.springboottest.controller;

import com.example.springboottest.common.Result;
import com.example.springboottest.config.AdminAuthInterceptor;
import com.example.springboottest.dto.AdminCategoryRequest;
import com.example.springboottest.dto.AdminItemRequest;
import com.example.springboottest.dto.LoginRequest;
import com.example.springboottest.service.AdminService;
import com.example.springboottest.vo.AdminCategoryVO;
import com.example.springboottest.vo.AdminItemVO;
import com.example.springboottest.vo.AdminLoginVO;
import com.example.springboottest.vo.AdminStatsVO;
import com.example.springboottest.vo.PageVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 后台管理接口
 *
 * 除登录接口外，所有 /api/admin/** 都要在请求头带 X-Admin-Token，
 * 没带或过期会被 AdminAuthInterceptor 拦下（返回 code 401）。
 *
 * 前端（Vite 代理 /api -> localhost:8080）调用：
 *   POST   /api/admin/auth/login              登录
 *   GET    /api/admin/auth/me                 当前登录信息（刷新页面时校验）
 *   POST   /api/admin/auth/logout             退出
 *   GET    /api/admin/stats                   概览统计
 *   GET    /api/admin/items                   内容列表（分页 + 筛选）
 *   GET    /api/admin/items/{id}              内容详情
 *   POST   /api/admin/items                   新增内容
 *   PUT    /api/admin/items/{id}              编辑内容
 *   DELETE /api/admin/items/{id}              删除内容
 *   GET    /api/admin/categories              分类列表
 *   POST   /api/admin/categories              新增分类
 *   PUT    /api/admin/categories/{id}         编辑分类
 *   DELETE /api/admin/categories/{id}         删除分类
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // ==================================================================
    //  登录
    // ==================================================================

    /**
     * 管理员登录
     * 请求体：{ "username": "admin", "password": "admin123" }
     */
    @PostMapping("/auth/login")
    public Result<AdminLoginVO> login(@Valid @RequestBody LoginRequest request) {
        return Result.success("登录成功", adminService.login(request.getUsername(), request.getPassword()));
    }

    /** 用 token 换当前管理员信息，前端刷新后台页面时调用，token 失效会返回 401 */
    @GetMapping("/auth/me")
    public Result<AdminLoginVO> me(
            @RequestHeader(value = AdminAuthInterceptor.TOKEN_HEADER, required = false) String token) {
        return Result.success(adminService.current(token));
    }

    /** 退出登录：立即让 token 失效 */
    @PostMapping("/auth/logout")
    public Result<Void> logout(
            @RequestHeader(value = AdminAuthInterceptor.TOKEN_HEADER, required = false) String token) {
        adminService.logout(token);
        return Result.success("已退出后台", null);
    }

    // ==================================================================
    //  概览
    // ==================================================================

    @GetMapping("/stats")
    public Result<AdminStatsVO> stats() {
        return Result.success(adminService.stats());
    }

    // ==================================================================
    //  内容管理
    // ==================================================================

    /**
     * 内容列表
     *
     * @param keyword    标题 / 简介 / 标签 模糊搜索
     * @param categoryId 分类筛选（一级分类会带上它的二级分类）
     * @param status     1 只看上架，0 只看下架，不传是全部
     */
    @GetMapping("/items")
    public Result<PageVO<AdminItemVO>> items(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        return Result.success(adminService.searchItems(keyword, categoryId, status, page, size));
    }

    /** 单条内容详情（编辑弹窗回填用） */
    @GetMapping("/items/{id}")
    public Result<AdminItemVO> itemDetail(@PathVariable Long id) {
        return Result.success(adminService.getItem(id));
    }

    /** 新增内容，返回新内容的 ID */
    @PostMapping("/items")
    public Result<Long> createItem(@Valid @RequestBody AdminItemRequest request) {
        return Result.success("新增成功", adminService.createItem(request));
    }

    /** 编辑内容（全量覆盖） */
    @PutMapping("/items/{id}")
    public Result<Void> updateItem(@PathVariable Long id, @Valid @RequestBody AdminItemRequest request) {
        adminService.updateItem(id, request);
        return Result.success("保存成功", null);
    }

    /** 删除内容（逻辑删除，数据库里还留着） */
    @DeleteMapping("/items/{id}")
    public Result<Void> deleteItem(@PathVariable Long id) {
        adminService.deleteItem(id);
        return Result.success("删除成功", null);
    }

    // ==================================================================
    //  分类管理
    // ==================================================================

    /** 全部分类（平铺，一级后面紧跟它的子分类） */
    @GetMapping("/categories")
    public Result<List<AdminCategoryVO>> categories() {
        return Result.success(adminService.listCategories());
    }

    @PostMapping("/categories")
    public Result<Long> createCategory(@Valid @RequestBody AdminCategoryRequest request) {
        return Result.success("新增成功", adminService.createCategory(request));
    }

    @PutMapping("/categories/{id}")
    public Result<Void> updateCategory(@PathVariable Long id, @Valid @RequestBody AdminCategoryRequest request) {
        adminService.updateCategory(id, request);
        return Result.success("保存成功", null);
    }

    @DeleteMapping("/categories/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        adminService.deleteCategory(id);
        return Result.success("删除成功", null);
    }
}
