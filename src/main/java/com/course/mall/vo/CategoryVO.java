package com.course.mall.vo;

import com.course.mall.entity.Category;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CategoryVO {
    private Long id;
    private Long parentId;
    private String name;
    private Integer sort;
    private String status;
    private List<CategoryVO> children = new ArrayList<>();

    public static CategoryVO from(Category category) {
        CategoryVO vo = new CategoryVO();
        vo.setId(category.getId());
        vo.setParentId(category.getParentId());
        vo.setName(category.getName());
        vo.setSort(category.getSort());
        vo.setStatus(category.getStatus());
        return vo;
    }
}
