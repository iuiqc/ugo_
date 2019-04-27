package com.ugo.item.controller;

import com.ugo.item.pojo.Category;
import com.ugo.item.service.CategoryService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author: qc
 * @Date: 2019-4-22 16:08
 */
@RestController
@RequestMapping("category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @GetMapping("list")
    /**
     * 根据parentId查询类目
     *@param [pid]
     *@return
     */
    public ResponseEntity<List<Category>> findCategoryByPid(
            @RequestParam(value = "pid", defaultValue = "0") Long pid) {

        List<Category> categoryList = categoryService.findCategoryByPid(pid);

        //没有查到返回404
        if (categoryList == null || categoryList.size() == 0) {
            return ResponseEntity.notFound().build();//new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(categoryList);
    }
    /**
     * @Author: qc
     * @Date: 2019/4/22 21:16
     */
    /**
     * 根据商品分类id查询名称
     * @param ids 要查询的分类id集合
     * @return 多个名称的集合
     */
    @GetMapping("names")
    public ResponseEntity<List<String>> queryCategoryNameById(@RequestParam("ids") List<Long> ids) {
        List<String> nameList = this.categoryService.queryCategoryNameByCids(ids);

        if (nameList == null || nameList.size() == 0) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return ResponseEntity.ok(nameList);

    }


    /**
     * @Author: qc
     * @Date: 2019/4/21 14:07
     */
    @GetMapping("bid/{bid}")
    public ResponseEntity<List<Category>> queryCategoryByBrandId(@PathVariable("bid") Long bid) {
        List<Category> list=this.categoryService.findCategoryByBrandId(bid);
        if(list == null || list.size() < 1){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(list);
    }


}
