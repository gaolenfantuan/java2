package com.example.springboottest.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboottest.entity.Category;
import com.example.springboottest.entity.HomeItem;
import com.example.springboottest.mapper.CategoryMapper;
import com.example.springboottest.mapper.HomeItemMapper;
import com.example.springboottest.service.HomeService;
import com.example.springboottest.vo.CategoryVO;
import com.example.springboottest.vo.HomeItemVO;
import com.example.springboottest.vo.HomeStatsVO;
import com.example.springboottest.vo.PageVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 首页业务实现
 *
 * 分类数据量很小（十几个），所以全部一次性查出来在内存里组装树 / 找子分类，
 * 比递归查库简单得多，也不会出现 N+1 查询。
 */
@Service
public class HomeServiceImpl implements HomeService {

    /** 默认每页条数 */
    private static final int DEFAULT_SIZE = 12;
    /** 每页最多条数，防止前端传个 99999 把库拖死 */
    private static final int MAX_SIZE = 50;

    private final CategoryMapper categoryMapper;
    private final HomeItemMapper homeItemMapper;

    public HomeServiceImpl(CategoryMapper categoryMapper, HomeItemMapper homeItemMapper) {
        this.categoryMapper = categoryMapper;
        this.homeItemMapper = homeItemMapper;
    }

    // ==================================================================
    //  分类
    // ==================================================================

    @Override
    public List<CategoryVO> getCategoryTree() {
        List<Category> all = loadCategories();
        Map<Long, List<Category>> byParent = groupByParent(all);
        Map<Long, Long> directCount = countDirectly();

        List<CategoryVO> roots = new ArrayList<>();
        for (Category root : byParent.getOrDefault(0L, List.of())) {
            roots.add(buildNode(root, byParent, directCount));
        }
        return roots;
    }

    /** 递归把分类组装成树，并把子分类的数量累加到父分类上 */
    private CategoryVO buildNode(Category category, Map<Long, List<Category>> byParent, Map<Long, Long> directCount) {
        CategoryVO vo = new CategoryVO();
        vo.setId(category.getId());
        vo.setName(category.getName());
        vo.setCode(category.getCode());
        vo.setIcon(category.getIcon());
        vo.setParentId(category.getParentId());
        vo.setLevel(category.getLevel());

        long total = directCount.getOrDefault(category.getId(), 0L);
        for (Category child : byParent.getOrDefault(category.getId(), List.of())) {
            CategoryVO childVO = buildNode(child, byParent, directCount);
            vo.getChildren().add(childVO);
            total += childVO.getCount();
        }
        vo.setCount(total);
        return vo;
    }

    // ==================================================================
    //  搜索 / 列表
    // ==================================================================

    @Override
    public PageVO<HomeItemVO> search(String keyword, Long categoryId, Boolean includeChildren,
                                     String sort, Integer page, Integer size) {
        long current = (page == null || page < 1) ? 1 : page;
        long pageSize = (size == null || size < 1) ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);

        List<Category> all = loadCategories();
        Map<Long, List<Category>> byParent = groupByParent(all);
        Map<Long, Category> byId = all.stream().collect(Collectors.toMap(Category::getId, Function.identity()));

        LambdaQueryWrapper<HomeItem> wrapper = Wrappers.<HomeItem>lambdaQuery()
                .eq(HomeItem::getStatus, 1);

        // 1. 分类筛选：选一级分类时默认把它下面的二级分类一起算进来
        if (categoryId != null && categoryId > 0) {
            Set<Long> categoryIds = Boolean.FALSE.equals(includeChildren)
                    ? Set.of(categoryId)
                    : collectSelfAndDescendants(categoryId, byParent);
            wrapper.in(HomeItem::getCategoryId, categoryIds);
        }

        // 2. 关键词
        String kw = keyword == null ? "" : keyword.trim();
        if (!kw.isEmpty()) {
            List<String> terms = Arrays.stream(kw.split("\\s+"))
                    .filter(t -> !t.isBlank())
                    .toList();
            applyKeyword(wrapper, terms, all, byParent);
        }

        // 3. 排序
        String sortType = sort == null ? "default" : sort.toLowerCase();
        switch (sortType) {
            case "hot" -> wrapper.orderByDesc(HomeItem::getLikes)
                    .orderByDesc(HomeItem::getViews)
                    .orderByAsc(HomeItem::getId);
            case "new" -> wrapper.orderByDesc(HomeItem::getCreateTime)
                    .orderByDesc(HomeItem::getId);
            default -> wrapper.orderByAsc(HomeItem::getSort)
                    .orderByAsc(HomeItem::getId);
        }

        Page<HomeItem> result = homeItemMapper.selectPage(new Page<>(current, pageSize), wrapper);
        List<HomeItemVO> list = result.getRecords().stream()
                .map(item -> toVO(item, byId))
                .toList();

        return new PageVO<>(list, result.getTotal(), current, pageSize);
    }

    /**
     * 关键词条件（分级分类搜索的核心）
     *
     * 规则：
     *   1. 多个词之间是「且」——「草莓 蛋糕」要求两个词都命中；
     *   2. 单个词的命中范围 = 标题 / 简介 / 标签 任一包含它，
     *      或者该词本身是分类名（如「甜点」「蛋糕」），命中这个分类及其所有子分类下的内容。
     *
     * 生成的 SQL 形如：
     *   AND (title LIKE ? OR summary LIKE ? OR tag LIKE ? OR category_id IN (1,11,12,13))
     *   AND (title LIKE ? OR summary LIKE ? OR tag LIKE ? OR category_id IN (11))
     */
    private void applyKeyword(LambdaQueryWrapper<HomeItem> wrapper, List<String> terms,
                              List<Category> all, Map<Long, List<Category>> byParent) {
        for (String term : terms) {
            // 这个词命中了哪些分类（含子分类）
            Set<Long> matchedCategoryIds = new LinkedHashSet<>();
            for (Category category : all) {
                if (category.getName().contains(term)) {
                    matchedCategoryIds.addAll(collectSelfAndDescendants(category.getId(), byParent));
                }
            }

            wrapper.and(w -> {
                w.like(HomeItem::getTitle, term)
                        .or().like(HomeItem::getSummary, term)
                        .or().like(HomeItem::getTag, term);
                if (!matchedCategoryIds.isEmpty()) {
                    w.or().in(HomeItem::getCategoryId, matchedCategoryIds);
                }
            });
        }
    }

    // ==================================================================
    //  详情
    // ==================================================================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HomeItemVO getDetail(Long id) {
        HomeItem item = homeItemMapper.selectOne(
                Wrappers.<HomeItem>lambdaQuery()
                        .eq(HomeItem::getId, id)
                        .eq(HomeItem::getStatus, 1)
        );
        if (item == null) {
            throw new IllegalArgumentException("这条内容已经不见啦，看看别的吧~");
        }

        // 浏览量 +1（直接让数据库自增，避免并发覆盖）
        homeItemMapper.update(null, Wrappers.<HomeItem>lambdaUpdate()
                .setSql("views = views + 1")
                .eq(HomeItem::getId, id));
        item.setViews((item.getViews() == null ? 0 : item.getViews()) + 1);

        List<Category> all = loadCategories();
        Map<Long, Category> byId = all.stream().collect(Collectors.toMap(Category::getId, Function.identity()));
        return toVO(item, byId);
    }

    // ==================================================================
    //  统计
    // ==================================================================

    @Override
    public HomeStatsVO getStats() {
        List<HomeItem> items = homeItemMapper.selectList(
                Wrappers.<HomeItem>lambdaQuery()
                        .select(HomeItem::getLikes, HomeItem::getIsHot)
                        .eq(HomeItem::getStatus, 1)
        );

        long likeTotal = 0;
        long hotTotal = 0;
        for (HomeItem item : items) {
            likeTotal += item.getLikes() == null ? 0 : item.getLikes();
            if (item.getIsHot() != null && item.getIsHot() == 1) {
                hotTotal++;
            }
        }

        Long categoryTotal = categoryMapper.selectCount(
                Wrappers.<Category>lambdaQuery().eq(Category::getStatus, 1)
        );

        return new HomeStatsVO((long) items.size(), categoryTotal, likeTotal, hotTotal);
    }

    // ==================================================================
    //  内部工具
    // ==================================================================

    /** 查询所有启用中的分类（按排序号、ID 升序） */
    private List<Category> loadCategories() {
        return categoryMapper.selectList(
                Wrappers.<Category>lambdaQuery()
                        .eq(Category::getStatus, 1)
                        .orderByAsc(Category::getSort)
                        .orderByAsc(Category::getId)
        );
    }

    /** 按 parentId 分组，方便快速找「某个分类的子分类」 */
    private Map<Long, List<Category>> groupByParent(List<Category> all) {
        Map<Long, List<Category>> byParent = new LinkedHashMap<>();
        for (Category category : all) {
            Long parentId = category.getParentId() == null ? 0L : category.getParentId();
            byParent.computeIfAbsent(parentId, k -> new ArrayList<>()).add(category);
        }
        return byParent;
    }

    /** 收集「自己 + 所有后代分类」的 ID（广度优先，天然支持以后加三级分类） */
    private Set<Long> collectSelfAndDescendants(Long rootId, Map<Long, List<Category>> byParent) {
        Set<Long> ids = new LinkedHashSet<>();
        Deque<Long> queue = new ArrayDeque<>();
        queue.add(rootId);
        while (!queue.isEmpty()) {
            Long id = queue.poll();
            // add 返回 false 说明已经处理过（脏数据成环时防止死循环）
            if (!ids.add(id)) {
                continue;
            }
            for (Category child : byParent.getOrDefault(id, List.of())) {
                queue.add(child.getId());
            }
        }
        return ids;
    }

    /** 每个分类「直接挂在它下面」的内容数量（不含子分类） */
    private Map<Long, Long> countDirectly() {
        List<HomeItem> items = homeItemMapper.selectList(
                Wrappers.<HomeItem>lambdaQuery()
                        .select(HomeItem::getCategoryId)
                        .eq(HomeItem::getStatus, 1)
        );
        Map<Long, Long> counts = new HashMap<>();
        for (HomeItem item : items) {
            if (item.getCategoryId() != null) {
                counts.merge(item.getCategoryId(), 1L, Long::sum);
            }
        }
        return counts;
    }

    /** 实体 -> VO，并把分类 ID 翻译成「一级分类名 + 二级分类名」 */
    private HomeItemVO toVO(HomeItem item, Map<Long, Category> byId) {
        HomeItemVO vo = new HomeItemVO();
        vo.setId(item.getId());
        vo.setTitle(item.getTitle());
        vo.setDesc(item.getSummary());
        vo.setEmoji(item.getEmoji());
        vo.setTag(item.getTag());
        vo.setBg(item.getCoverBg());
        vo.setCategoryId(item.getCategoryId());
        vo.setLikes(item.getLikes());
        vo.setViews(item.getViews());
        vo.setHot(item.getIsHot() != null && item.getIsHot() == 1);

        Category sub = byId.get(item.getCategoryId());
        if (sub != null) {
            vo.setSubCategory(sub.getName());
            Category parent = (sub.getParentId() != null && sub.getParentId() > 0)
                    ? byId.get(sub.getParentId())
                    : null;
            // 卡片角标显示的是一级分类（甜点/穿搭/好物/心情），没有父级就用自己
            vo.setCategory(parent != null ? parent.getName() : sub.getName());
        }
        return vo;
    }
}
