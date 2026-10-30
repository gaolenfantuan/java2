package com.example.springboottest.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboottest.dto.AdminCategoryRequest;
import com.example.springboottest.dto.AdminItemRequest;
import com.example.springboottest.entity.Admin;
import com.example.springboottest.entity.Category;
import com.example.springboottest.entity.HomeItem;
import com.example.springboottest.mapper.AdminMapper;
import com.example.springboottest.mapper.CategoryMapper;
import com.example.springboottest.mapper.HomeItemMapper;
import com.example.springboottest.security.AdminTokenStore;
import com.example.springboottest.service.AdminService;
import com.example.springboottest.util.PasswordUtil;
import com.example.springboottest.vo.AdminCategoryVO;
import com.example.springboottest.vo.AdminItemVO;
import com.example.springboottest.vo.AdminLoginVO;
import com.example.springboottest.vo.AdminStatsVO;
import com.example.springboottest.vo.PageVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 后台管理业务实现
 *
 * 和 HomeService 的区别：
 *   HomeService   只查「已上架 + 已启用」的数据，给普通用户看；
 *   AdminService  要能看到全部（含下架的、隐藏的），并且能改。
 */
@Service
public class AdminServiceImpl implements AdminService {

    /** 后台表格每页最多 100 条 */
    private static final int MAX_SIZE = 100;
    /** 新增内容时如果没填封面，就用这个渐变 */
    private static final String DEFAULT_COVER_BG = "linear-gradient(135deg,#ffe3ef,#ffc9de)";
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final AdminMapper adminMapper;
    private final CategoryMapper categoryMapper;
    private final HomeItemMapper homeItemMapper;
    private final AdminTokenStore tokenStore;

    public AdminServiceImpl(AdminMapper adminMapper,
                            CategoryMapper categoryMapper,
                            HomeItemMapper homeItemMapper,
                            AdminTokenStore tokenStore) {
        this.adminMapper = adminMapper;
        this.categoryMapper = categoryMapper;
        this.homeItemMapper = homeItemMapper;
        this.tokenStore = tokenStore;
    }

    // ==================================================================
    //  登录
    // ==================================================================

    @Override
    public AdminLoginVO login(String username, String password) {
        Admin admin = adminMapper.selectOne(
                Wrappers.<Admin>lambdaQuery().eq(Admin::getUsername, username == null ? "" : username.trim())
        );
        // 账号不存在和密码错误给同一个提示，避免被撞库试出账号
        if (admin == null || !PasswordUtil.matches(password, admin.getPassword())) {
            throw new IllegalArgumentException("账号或密码不对哦，再检查一下~");
        }
        if (admin.getStatus() != null && admin.getStatus() == 0) {
            throw new IllegalArgumentException("这个管理员账号已被禁用啦，请联系超级管理员~");
        }

        AdminTokenStore.Session session = tokenStore.issue(admin.getId(), admin.getUsername(), admin.getNickname());
        return new AdminLoginVO(session.getToken(), admin.getId(), admin.getUsername(),
                admin.getNickname(), session.getExpiresIn());
    }

    @Override
    public AdminLoginVO current(String token) {
        AdminTokenStore.Session session = tokenStore.get(token);
        if (session == null) {
            throw new IllegalArgumentException("登录已过期，请重新登录后台~");
        }
        return new AdminLoginVO(session.getToken(), session.getAdminId(), session.getUsername(),
                session.getNickname(), session.getExpiresIn());
    }

    @Override
    public void logout(String token) {
        tokenStore.revoke(token);
    }

    // ==================================================================
    //  内容管理
    // ==================================================================

    @Override
    public PageVO<AdminItemVO> searchItems(String keyword, Long categoryId, Integer status,
                                           Integer page, Integer size) {
        long current = (page == null || page < 1) ? 1 : page;
        long pageSize = (size == null || size < 1) ? 10 : Math.min(size, MAX_SIZE);

        // 后台要看到全部分类（包括隐藏的），所以这里不筛 status
        List<Category> allCategories = loadAllCategories();
        Map<Long, Category> byId = allCategories.stream()
                .collect(Collectors.toMap(Category::getId, Function.identity()));

        LambdaQueryWrapper<HomeItem> wrapper = Wrappers.<HomeItem>lambdaQuery();

        // 状态筛选：不传就是全部
        if (status != null) {
            wrapper.eq(HomeItem::getStatus, status);
        }

        // 分类筛选：选一级分类时连同它下面的二级分类一起看
        if (categoryId != null && categoryId > 0) {
            Set<Long> ids = new LinkedHashSet<>();
            ids.add(categoryId);
            for (Category category : allCategories) {
                if (categoryId.equals(category.getParentId())) {
                    ids.add(category.getId());
                }
            }
            wrapper.in(HomeItem::getCategoryId, ids);
        }

        String kw = keyword == null ? "" : keyword.trim();
        if (!kw.isEmpty()) {
            wrapper.and(w -> w.like(HomeItem::getTitle, kw)
                    .or().like(HomeItem::getSummary, kw)
                    .or().like(HomeItem::getTag, kw));
        }

        // 后台固定按「排序号 + ID」，方便运营自己调整顺序
        wrapper.orderByAsc(HomeItem::getSort).orderByAsc(HomeItem::getId);

        Page<HomeItem> result = homeItemMapper.selectPage(new Page<>(current, pageSize), wrapper);
        List<AdminItemVO> list = result.getRecords().stream()
                .map(item -> toItemVO(item, byId))
                .toList();

        return new PageVO<>(list, result.getTotal(), current, pageSize);
    }

    @Override
    public AdminItemVO getItem(Long id) {
        HomeItem item = homeItemMapper.selectById(id);
        if (item == null) {
            throw new IllegalArgumentException("这条内容不存在或已被删除~");
        }
        Map<Long, Category> byId = loadAllCategories().stream()
                .collect(Collectors.toMap(Category::getId, Function.identity()));
        return toItemVO(item, byId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createItem(AdminItemRequest request) {
        requireExistingCategory(request.getCategoryId());

        HomeItem item = new HomeItem();
        item.setTitle(request.getTitle().trim());
        item.setSummary(trimToNull(request.getSummary()));
        item.setEmoji(blankToDefault(request.getEmoji(), "🎀"));
        item.setTag(trimToNull(request.getTag()));
        item.setCoverBg(blankToDefault(request.getCoverBg(), DEFAULT_COVER_BG));
        item.setCategoryId(request.getCategoryId());
        item.setLikes(nullToZero(request.getLikes()));
        item.setViews(nullToZero(request.getViews()));
        item.setIsHot(request.getIsHot() == null ? 0 : request.getIsHot());
        item.setSort(request.getSort() == null ? nextItemSort() : request.getSort());
        item.setStatus(request.getStatus() == null ? 1 : request.getStatus());

        homeItemMapper.insert(item);
        return item.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateItem(Long id, AdminItemRequest request) {
        HomeItem exist = homeItemMapper.selectById(id);
        if (exist == null) {
            throw new IllegalArgumentException("这条内容不存在或已被删除~");
        }
        requireExistingCategory(request.getCategoryId());

        // 用 set(...) 逐个赋值（而不是 updateById），这样「把简介/标签清空」也能真正写进数据库
        homeItemMapper.update(null, Wrappers.<HomeItem>lambdaUpdate()
                .set(HomeItem::getTitle, request.getTitle().trim())
                .set(HomeItem::getSummary, trimToNull(request.getSummary()))
                .set(HomeItem::getEmoji, blankToDefault(request.getEmoji(), "🎀"))
                .set(HomeItem::getTag, trimToNull(request.getTag()))
                .set(HomeItem::getCoverBg, blankToDefault(request.getCoverBg(), DEFAULT_COVER_BG))
                .set(HomeItem::getCategoryId, request.getCategoryId())
                .set(HomeItem::getLikes, nullToZero(request.getLikes()))
                .set(HomeItem::getViews, nullToZero(request.getViews()))
                .set(HomeItem::getIsHot, request.getIsHot() == null ? 0 : request.getIsHot())
                .set(HomeItem::getSort, request.getSort() == null ? 0 : request.getSort())
                .set(HomeItem::getStatus, request.getStatus() == null ? 1 : request.getStatus())
                .set(HomeItem::getUpdateTime, LocalDateTime.now())
                .eq(HomeItem::getId, id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteItem(Long id) {
        HomeItem exist = homeItemMapper.selectById(id);
        if (exist == null) {
            throw new IllegalArgumentException("这条内容不存在或已被删除~");
        }
        // 逻辑删除：数据库里 deleted 置 1，首页就查不到了（数据还在，方便以后做回收站）
        homeItemMapper.deleteById(id);
    }

    // ==================================================================
    //  分类管理
    // ==================================================================

    @Override
    public List<AdminCategoryVO> listCategories() {
        List<Category> all = loadAllCategories();
        Map<Long, Category> byId = all.stream()
                .collect(Collectors.toMap(Category::getId, Function.identity()));
        Map<Long, Long> counts = countItemsByCategory();

        // 排成「一级分类 -> 它的二级分类 -> 下一个一级分类」的顺序，前端表格直接按数组顺序渲染即可
        Map<Long, List<Category>> byParent = new LinkedHashMap<>();
        for (Category category : all) {
            Long parentId = category.getParentId() == null ? 0L : category.getParentId();
            byParent.computeIfAbsent(parentId, k -> new ArrayList<>()).add(category);
        }
        List<Category> ordered = new ArrayList<>();
        Set<Long> added = new LinkedHashSet<>();
        for (Category root : byParent.getOrDefault(0L, List.of())) {
            if (added.add(root.getId())) {
                ordered.add(root);
            }
            for (Category child : byParent.getOrDefault(root.getId(), List.of())) {
                if (added.add(child.getId())) {
                    ordered.add(child);
                }
            }
        }
        // 父分类被删掉的「孤儿分类」放最后，避免数据异常时看不见
        for (Category category : all) {
            if (added.add(category.getId())) {
                ordered.add(category);
            }
        }

        List<AdminCategoryVO> list = new ArrayList<>(ordered.size());
        for (Category category : ordered) {
            AdminCategoryVO vo = new AdminCategoryVO();
            vo.setId(category.getId());
            vo.setName(category.getName());
            vo.setCode(category.getCode());
            vo.setParentId(category.getParentId());
            vo.setLevel(category.getLevel());
            vo.setIcon(category.getIcon());
            vo.setSort(category.getSort());
            vo.setStatus(category.getStatus());
            vo.setItemCount(counts.getOrDefault(category.getId(), 0L));
            vo.setCreateTime(formatTime(category.getCreateTime()));
            Long parentId = category.getParentId();
            if (parentId != null && parentId > 0) {
                Category parent = byId.get(parentId);
                vo.setParentName(parent == null ? "已删除的分类" : parent.getName());
            }
            list.add(vo);
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCategory(AdminCategoryRequest request) {
        String name = request.getName().trim();
        long parentId = request.getParentId() == null ? 0L : request.getParentId();

        Category parent = null;
        if (parentId > 0) {
            parent = categoryMapper.selectById(parentId);
            if (parent == null) {
                throw new IllegalArgumentException("上级分类不存在，刷新一下页面试试~");
            }
            if (parent.getLevel() != null && parent.getLevel() >= 2) {
                throw new IllegalArgumentException("目前最多只支持两级分类，二级分类下面不能再加子分类啦~");
            }
        }

        // 同一级下不允许重名
        Long duplicated = categoryMapper.selectCount(Wrappers.<Category>lambdaQuery()
                .eq(Category::getName, name)
                .eq(Category::getParentId, parentId));
        if (duplicated != null && duplicated > 0) {
            throw new IllegalArgumentException("同一级下已经有叫「" + name + "」的分类啦~");
        }

        String code = trimToNull(request.getCode());
        if (code == null) {
            // 没填就自动生成一个，保证唯一索引不会炸
            code = "c" + System.currentTimeMillis();
        } else {
            requireValidCode(code);
            Long codeCount = categoryMapper.selectCount(Wrappers.<Category>lambdaQuery()
                    .eq(Category::getCode, code));
            if (codeCount != null && codeCount > 0) {
                throw new IllegalArgumentException("英文标识「" + code + "」已经被别的分类用掉啦，换一个吧~");
            }
        }

        Category category = new Category();
        category.setName(name);
        category.setCode(code);
        category.setParentId(parentId);
        category.setLevel(parentId > 0 ? 2 : 1);
        category.setIcon(trimToNull(request.getIcon()));
        category.setSort(request.getSort() == null ? nextCategorySort(parentId) : request.getSort());
        category.setStatus(request.getStatus() == null ? 1 : request.getStatus());

        categoryMapper.insert(category);
        return category.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCategory(Long id, AdminCategoryRequest request) {
        Category exist = categoryMapper.selectById(id);
        if (exist == null) {
            throw new IllegalArgumentException("这个分类不存在或已被删除~");
        }

        String name = request.getName().trim();
        long parentId = request.getParentId() == null ? 0L : request.getParentId();

        if (parentId == id) {
            throw new IllegalArgumentException("上级分类不能选自己哦~");
        }

        Long childCount = categoryMapper.selectCount(Wrappers.<Category>lambdaQuery()
                .eq(Category::getParentId, id));
        if (childCount != null && childCount > 0 && parentId > 0) {
            throw new IllegalArgumentException("「" + exist.getName() + "」下面还有 "
                    + childCount + " 个子分类，不能把它改成二级分类，请先处理子分类~");
        }

        if (parentId > 0) {
            Category parent = categoryMapper.selectById(parentId);
            if (parent == null) {
                throw new IllegalArgumentException("上级分类不存在，刷新一下页面试试~");
            }
            if (parent.getLevel() != null && parent.getLevel() >= 2) {
                throw new IllegalArgumentException("目前最多只支持两级分类，二级分类下面不能再加子分类啦~");
            }
        }

        Long duplicated = categoryMapper.selectCount(Wrappers.<Category>lambdaQuery()
                .eq(Category::getName, name)
                .eq(Category::getParentId, parentId)
                .ne(Category::getId, id));
        if (duplicated != null && duplicated > 0) {
            throw new IllegalArgumentException("同一级下已经有叫「" + name + "」的分类啦~");
        }

        String code = trimToNull(request.getCode());
        if (code == null) {
            code = exist.getCode();
        } else {
            requireValidCode(code);
            Long codeCount = categoryMapper.selectCount(Wrappers.<Category>lambdaQuery()
                    .eq(Category::getCode, code)
                    .ne(Category::getId, id));
            if (codeCount != null && codeCount > 0) {
                throw new IllegalArgumentException("英文标识「" + code + "」已经被别的分类用掉啦，换一个吧~");
            }
        }

        categoryMapper.update(null, Wrappers.<Category>lambdaUpdate()
                .set(Category::getName, name)
                .set(Category::getCode, code)
                .set(Category::getParentId, parentId)
                .set(Category::getLevel, parentId > 0 ? 2 : 1)
                .set(Category::getIcon, trimToNull(request.getIcon()))
                .set(Category::getSort, request.getSort() == null ? 0 : request.getSort())
                .set(Category::getStatus, request.getStatus() == null ? 1 : request.getStatus())
                .set(Category::getUpdateTime, LocalDateTime.now())
                .eq(Category::getId, id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long id) {
        Category exist = categoryMapper.selectById(id);
        if (exist == null) {
            throw new IllegalArgumentException("这个分类不存在或已被删除~");
        }

        Long childCount = categoryMapper.selectCount(Wrappers.<Category>lambdaQuery()
                .eq(Category::getParentId, id));
        if (childCount != null && childCount > 0) {
            throw new IllegalArgumentException("「" + exist.getName() + "」下面还有 " + childCount
                    + " 个子分类，先把子分类删掉再试~");
        }

        Long itemCount = homeItemMapper.selectCount(Wrappers.<HomeItem>lambdaQuery()
                .eq(HomeItem::getCategoryId, id));
        if (itemCount != null && itemCount > 0) {
            throw new IllegalArgumentException("「" + exist.getName() + "」下面还有 " + itemCount
                    + " 条内容，先把它们改到别的分类或删掉再试~");
        }

        categoryMapper.deleteById(id);
    }

    // ==================================================================
    //  概览
    // ==================================================================

    @Override
    public AdminStatsVO stats() {
        // 概览数字要看真实情况，所以连下架的内容一起统计
        List<HomeItem> items = homeItemMapper.selectList(Wrappers.<HomeItem>lambdaQuery()
                .select(HomeItem::getStatus, HomeItem::getLikes, HomeItem::getViews, HomeItem::getIsHot));

        long online = 0, offline = 0, likeTotal = 0, viewTotal = 0, hotTotal = 0;
        for (HomeItem item : items) {
            if (item.getStatus() != null && item.getStatus() == 1) {
                online++;
            } else {
                offline++;
            }
            likeTotal += item.getLikes() == null ? 0 : item.getLikes();
            viewTotal += item.getViews() == null ? 0 : item.getViews();
            if (item.getIsHot() != null && item.getIsHot() == 1) {
                hotTotal++;
            }
        }

        Long categoryTotal = categoryMapper.selectCount(Wrappers.<Category>lambdaQuery());

        return new AdminStatsVO((long) items.size(), online, offline, categoryTotal,
                likeTotal, viewTotal, hotTotal);
    }

    // ==================================================================
    //  内部工具
    // ==================================================================

    /** 查全部分类（不过滤状态，后台管理需要看到隐藏的分类） */
    private List<Category> loadAllCategories() {
        return categoryMapper.selectList(Wrappers.<Category>lambdaQuery()
                .orderByAsc(Category::getSort)
                .orderByAsc(Category::getId));
    }

    /** 每个分类直接挂了多少条内容（含下架内容） */
    private Map<Long, Long> countItemsByCategory() {
        List<HomeItem> items = homeItemMapper.selectList(Wrappers.<HomeItem>lambdaQuery()
                .select(HomeItem::getCategoryId));
        Map<Long, Long> counts = new HashMap<>();
        for (HomeItem item : items) {
            if (item.getCategoryId() != null) {
                counts.merge(item.getCategoryId(), 1L, Long::sum);
            }
        }
        return counts;
    }

    /** 新增内容时，没填排序号就排在最后一名的后面 */
    private int nextItemSort() {
        List<HomeItem> items = homeItemMapper.selectList(Wrappers.<HomeItem>lambdaQuery()
                .select(HomeItem::getSort)
                .orderByDesc(HomeItem::getSort)
                .last("LIMIT 1"));
        if (items.isEmpty() || items.get(0).getSort() == null) {
            return 1;
        }
        return items.get(0).getSort() + 1;
    }

    /** 新增分类时，没填排序号就排在同级最后 */
    private int nextCategorySort(long parentId) {
        List<Category> list = categoryMapper.selectList(Wrappers.<Category>lambdaQuery()
                .select(Category::getSort)
                .eq(Category::getParentId, parentId)
                .orderByDesc(Category::getSort)
                .last("LIMIT 1"));
        if (list.isEmpty() || list.get(0).getSort() == null) {
            return 1;
        }
        return list.get(0).getSort() + 1;
    }

    /** 内容必须挂在一个真实存在的分类上 */
    private void requireExistingCategory(Long categoryId) {
        if (categoryId == null) {
            throw new IllegalArgumentException("请选择所属分类~");
        }
        Category category = categoryMapper.selectById(categoryId);
        if (category == null) {
            throw new IllegalArgumentException("选择的分类不存在，刷新页面重新选一下~");
        }
    }

    /** 英文标识只允许字母开头 + 字母数字下划线 */
    private void requireValidCode(String code) {
        if (!code.matches("^[a-zA-Z][a-zA-Z0-9_]*$")) {
            throw new IllegalArgumentException("英文标识要以字母开头，只能用字母、数字、下划线~");
        }
    }

    /** 实体 -> 后台 VO */
    private AdminItemVO toItemVO(HomeItem item, Map<Long, Category> byId) {
        AdminItemVO vo = new AdminItemVO();
        vo.setId(item.getId());
        vo.setTitle(item.getTitle());
        vo.setSummary(item.getSummary());
        vo.setEmoji(item.getEmoji());
        vo.setTag(item.getTag());
        vo.setCoverBg(item.getCoverBg());
        vo.setCategoryId(item.getCategoryId());
        vo.setLikes(item.getLikes());
        vo.setViews(item.getViews());
        vo.setIsHot(item.getIsHot());
        vo.setSort(item.getSort());
        vo.setStatus(item.getStatus());
        vo.setCreateTime(formatTime(item.getCreateTime()));
        vo.setUpdateTime(formatTime(item.getUpdateTime()));

        Category sub = byId.get(item.getCategoryId());
        if (sub != null) {
            vo.setSubCategoryName(sub.getName());
            Category parent = (sub.getParentId() != null && sub.getParentId() > 0)
                    ? byId.get(sub.getParentId())
                    : sub;
            vo.setCategoryName(parent == null ? sub.getName() : parent.getName());
        }
        return vo;
    }

    private String formatTime(LocalDateTime time) {
        return time == null ? "" : time.format(TIME_FMT);
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String blankToDefault(String value, String defaultValue) {
        String trimmed = trimToNull(value);
        return trimmed == null ? defaultValue : trimmed;
    }

    private static int nullToZero(Integer value) {
        return value == null ? 0 : Math.max(0, value);
    }
}
