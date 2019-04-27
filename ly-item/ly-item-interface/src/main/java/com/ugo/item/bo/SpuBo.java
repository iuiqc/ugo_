package com.ugo.item.bo;

import com.ugo.item.pojo.Sku;
import com.ugo.item.pojo.Spu;
import com.ugo.item.pojo.SpuDetail;

import javax.persistence.Transient;
import java.util.List;

/**
 * @Author: qc
 * @Date: 2019/4/22 18:00
 * 页面展示的商品分类和品牌名称(数据库中保存的是id)
 */
public class SpuBo extends Spu {
    @Transient
    String cname;// 商品分类名称
    @Transient
    String bname;// 品牌名称
    @Transient
    SpuDetail spuDetail;// 商品详情
    @Transient
    List<Sku> skus;// sku列表

    public String getCname() {
        return cname;
    }

    public void setCname(String cname) {
        this.cname = cname;
    }

    public String getBname() {
        return bname;
    }

    public void setBname(String bname) {
        this.bname = bname;
    }

    public SpuDetail getSpuDetail() {
        return spuDetail;
    }

    public void setSpuDetail(SpuDetail spuDetail) {
        this.spuDetail = spuDetail;
    }

    public List<Sku> getSkus() {
        return skus;
    }

    public void setSkus(List<Sku> skus) {
        this.skus = skus;
    }
}
