package com.course.mall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.course.mall.common.BusinessException;
import com.course.mall.dto.CategoryRequest;
import com.course.mall.entity.Category;
import com.course.mall.entity.Product;
import com.course.mall.mapper.CategoryMapper;
import com.course.mall.mapper.ProductMapper;
import com.course.mall.vo.CategoryVO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CategoryService {
    private final CategoryMapper categoryMapper;
    private final ProductMapper productMapper;

    public CategoryService(CategoryMapper categoryMapper, ProductMapper productMapper) {
        this.categoryMapper = categoryMapper;
        this.productMapper = productMapper;
    }

    public List<CategoryVO> tree(boolean admin) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<Category>()
                .orderByAsc(Category::getSort)
                .orderByAsc(Category::getId);
        if (!admin) {
            wrapper.eq(Category::getStatus, "ON");
        }
        List<Category> categories = categoryMapper.selectList(wrapper);
        Map<Long, CategoryVO> map = new LinkedHashMap<>();
        for (Category category : categories) {
            map.put(category.getId(), CategoryVO.from(category));
        }
        List<CategoryVO> roots = new ArrayList<>();
        for (Category category : categories) {
            CategoryVO vo = map.get(category.getId());
            if (category.getParentId() == null || category.getParentId() == 0) {
                roots.add(vo);
            } else {
                CategoryVO parent = map.get(category.getParentId());
                if (parent != null) {
                    parent.getChildren().add(vo);
                } else {
                    roots.add(vo);
                }
            }
        }
        roots.sort(Comparator.comparing(CategoryVO::getSort));
        return roots;
    }

    public List<Category> listForAdmin() {
        return categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .orderByAsc(Category::getSort)
                .orderByAsc(Category::getId));
    }

    public Category create(CategoryRequest request) {
        Category category = new Category();
        fill(category, request);
        categoryMapper.insert(category);
        return category;
    }

    public Category update(Long id, CategoryRequest request) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw BusinessException.notFound("分类不存在");
        }
        fill(category, request);
        categoryMapper.updateById(category);
        return category;
    }

    public void delete(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw BusinessException.notFound("分类不存在");
        }
        Long childCount = categoryMapper.selectCount(new LambdaQueryWrapper<Category>()
                .eq(Category::getParentId, id));
        if (childCount > 0) {
            throw BusinessException.badRequest("请先删除或迁移子分类");
        }
        Long productCount = productMapper.selectCount(new LambdaQueryWrapper<Product>()
                .eq(Product::getCategoryId, id));
        if (productCount > 0) {
            throw BusinessException.badRequest("该分类下还有商品，不能删除");
        }
        categoryMapper.deleteById(id);
    }

    private void fill(Category category, CategoryRequest request) {
        category.setParentId(request.getParentId() == null ? 0L : request.getParentId());
        category.setName(request.getName());
        category.setSort(request.getSort() == null ? 0 : request.getSort());
        category.setStatus(request.getStatus() == null ? "ON" : request.getStatus());
    }
}
