package com.course.mall.controller;

import com.course.mall.common.Result;
import com.course.mall.service.CategoryService;
import com.course.mall.vo.CategoryVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/tree")
    public Result<List<CategoryVO>> tree() {
        return Result.ok(categoryService.tree(false));
    }
}
