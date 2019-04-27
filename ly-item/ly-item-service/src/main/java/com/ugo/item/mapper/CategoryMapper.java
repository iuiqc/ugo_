package com.ugo.item.mapper;

import com.ugo.item.pojo.Category;
import org.apache.ibatis.annotations.Select;
import org.springframework.data.jpa.repository.JpaRepository;
import tk.mybatis.mapper.additional.idlist.SelectByIdListMapper;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @Author: qc
 * @Date: 2019/4/21 17:06
 */
public interface CategoryMapper extends Mapper<Category>,SelectByIdListMapper<Category,Long> {

    /**
     * @Author: qc
     * @Date: 2019/4/21 17:06
     */
    /**
     *根据品牌id查询商品分类
     *@param bid
     *@return
     */
    @Select("SELECT * FROM tb_category WHERE id IN (SELECT category_id FROM tb_category_brand WHERE brand_id = #{bid}) ")
    List<Category> findCategoryByBrandId(Long bid);
}
