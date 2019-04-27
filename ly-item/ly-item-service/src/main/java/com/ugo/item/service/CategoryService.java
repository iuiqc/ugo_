package com.ugo.item.service;

import com.ugo.item.mapper.CategoryMapper;
import com.ugo.item.pojo.Category;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: qc
 * @Date: 2019/4/21 11:39
 */
@Service
public class CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;

//    public List<Category> findCategoryByPid(Long pid) {
//        Category category = new Category();
//        //查询使用通用mapper，由于条件不是id主键所以要封装对象中
//        category.setParentId(pid);
//
//        return categoryMapper.select(category);
//    }

    public List<String> queryCategoryNameByCids(List<Long> cids) {

//        List<Category> categoryList = categoryMapper.selectByIdList(cids);
//
//        List<String> names = new ArrayList<>();
//
//        for (Category category : categoryList) {
//            names.add(category.getName());
//        }
//
//
//        return names;
        return this.categoryMapper.selectByIdList(cids).stream().map(Category::getName).collect(Collectors.toList());
    }
    /**
     * @Author: qc
     * @Date: 2019/4/21 13:59
     */
    /**
     * @param pid
     * @return
     */
    public List<Category> findCategoryByPid(Long pid) {
        Category category = new Category();
        category.setParentId(pid);
        return this.categoryMapper.select(category);
    }

    /**
     * @Author: qc
     * @Date: 2019/4/22 18:19
     */
    public List<Category> findCategoryByBrandId(Long bid) {
        return this.categoryMapper.findCategoryByBrandId(bid);
    }


}
