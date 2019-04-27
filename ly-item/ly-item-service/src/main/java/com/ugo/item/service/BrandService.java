package com.ugo.item.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ugo.common.pojo.PageResult;
import com.ugo.item.mapper.BrandMapper;
import com.ugo.item.pojo.Brand;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * @Author: qc
 * @Date: 2019-4-22 17:54
 */
@Service
@Slf4j
public class BrandService {

    @Autowired
    private BrandMapper brandMapper;

    public PageResult<Brand> queryBrandByPageAndSort(Integer page, Integer rows, String sortBy, Boolean desc, String key) {

        //开启分页查询，page表示当前页，rows表示页容量
        PageHelper.startPage(page, rows);

        Example example = new Example(Brand.class);

        //包含排序字段
        if (StringUtils.isNotBlank(sortBy)) {

            //根据排序的升序或者降序来设置排序的方式
            String orderClause = sortBy + (desc ? " DESC" : " ASC");

            example.setOrderByClause(orderClause);//id ASC
        }

        if (StringUtils.isNotBlank(key)) {
            example.createCriteria().andLike("name", "%" + key + "%").orEqualTo("letter", key.toUpperCase());
        }

        Page<Brand> brands = (Page<Brand>) brandMapper.selectByExample(example);

        PageResult<Brand> pageResult = new PageResult<>(brands.getTotal(), brands);

        return pageResult;
    }

    public void saveBrand(Brand brand, List<Long> cids) {
        //由通用mapper保存brand品牌
        brandMapper.insert(brand);

        for (Long cid : cids) {
            brandMapper.insertCategoryAndBrand(cid, brand.getId());
        }
    }

    public List<Brand> queryBrandsByCategoryId(Long cid) {
        return brandMapper.queryBrandsByCategoryId(cid);
    }

    public List<Brand> queryBrandByIds(List<Long> ids) {
        return this.brandMapper.selectByIdList(ids);
    }

    public void deleteByBrandIdInCategoryBrand(Long bid) {
        this.brandMapper.deleteByBrandIdInCategoryBrand(bid);
    }

    public void updateBrand(Brand brand, List<Long> categories) {
        //删除原来的数据
        try {
            deleteByBrandIdInCategoryBrand(brand.getId());
        }catch (Exception e)
        {
            log.error("删除原来的数据");
        }

        // 修改品牌信息
        try {
            this.brandMapper.updateByPrimaryKeySelective(brand);
        } catch (Exception e) {
            log.error("修改品牌信息");
        }

        try {


            //维护品牌和分类中间表
            for (Long cid : categories) {
                System.out.println("cid:"+cid+",bid:"+brand.getId());
                this.brandMapper.insertCategoryBrand(cid, brand.getId());
            }
        } catch (Exception e) {
            log.error("维护品牌和分类中间表");

        }
        }
    /**
     * @Author: qc
     * @Date: 2019/4/21 23:00
     */
    public void deleteBrand(long bid) {
        this.brandMapper.deleteByPrimaryKey(bid);
        //维护中间表
        this.brandMapper.deleteByBrandIdInCategoryBrand(bid);
    }
}
