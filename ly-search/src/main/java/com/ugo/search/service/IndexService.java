package com.ugo.search.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ugo.common.utils.JsonUtils;
import com.ugo.item.bo.SpuBo;
import com.ugo.item.pojo.Sku;
import com.ugo.item.pojo.SpecParam;
import com.ugo.item.pojo.Spu;
import com.ugo.item.pojo.SpuDetail;
import com.ugo.search.client.CategoryClient;
import com.ugo.search.client.GoodsClient;
import com.ugo.search.client.SpecificationClient;
import com.ugo.search.pojo.Goods;
import com.ugo.search.repository.GoodsRepository;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

/**
 * @Author: qc
 * @Date: 2019/4/22 21:16
 */
@Service
public class IndexService {
    private ObjectMapper mapper = new ObjectMapper();
    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SpecificationClient specificationClient;

    @Autowired
    private GoodsRepository goodsRepository;

    public Goods buildGoods(SpuBo spu) throws IOException {
        Goods goods = new Goods();

        // 查询商品分类名称
        List<String> names = this.categoryClient.queryNameByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        // 查询sku
        List<Sku> skus = this.goodsClient.querySkuBySpuId(spu.getId());
        // 查询详情
        SpuDetail spuDetail = this.goodsClient.querySpuDetailById(spu.getId());
        // 查询规格参数
        List<SpecParam> params = this.specificationClient.querySpecParam(null, spu.getCid3(), true, null);

        // 处理sku，仅封装id、价格、标题、图片，并获得价格集合
        List<Long> prices = new ArrayList<>();
        List<Map<String, Object>> skuList = new ArrayList<>();
        skus.forEach(sku -> {
            prices.add(sku.getPrice());
            Map<String, Object> skuMap = new HashMap<>();
            skuMap.put("id", sku.getId());
            skuMap.put("title", sku.getTitle());
            skuMap.put("price", sku.getPrice());
            skuMap.put("image", StringUtils.isBlank(sku.getImages()) ? "" : StringUtils.split(sku.getImages(), ",")[0]);
            skuList.add(skuMap);
        });

        // 处理规格参数
        Map<String, Object> genericSpecs = mapper.readValue(spuDetail.getGenericSpec(), new TypeReference<Map<String, Object>>() {
        });
        Map<String, Object> specialSpecs = mapper.readValue(spuDetail.getSpecialSpec(), new TypeReference<Map<String, Object>>() {
        });
        // 获取可搜索的规格参数
        Map<String, Object> searchSpec = new HashMap<>();

        // 过滤规格模板，把所有可搜索的信息保存到Map中
        Map<String, Object> specMap = new HashMap<>();
        params.forEach(p -> {
            if (p.getSearching()) {
                if (p.getGeneric()) {
                    String value = genericSpecs.get(p.getId().toString()).toString();
                    if(p.getNumeric()){
                        value = chooseSegment(value, p);
                    }
                    specMap.put(p.getName(), StringUtils.isBlank(value) ? "其它" : value);
                } else {
                    specMap.put(p.getName(), specialSpecs.get(p.getId().toString()));
                }
            }
        });

        goods.setId(spu.getId());
        goods.setSubTitle(spu.getSubTitle());
        goods.setBrandId(spu.getBrandId());
        goods.setCid1(spu.getCid1());
        goods.setCid2(spu.getCid2());
        goods.setCid3(spu.getCid3());
        goods.setCreateTime(spu.getCreateTime());
        goods.setAll(spu.getTitle() + " " + StringUtils.join(names, " "));
        goods.setPrice(prices);
        goods.setSkus(mapper.writeValueAsString(skuList));
        goods.setSpecs(specMap);
        return goods;
//        Long id = spu.getId();
//        //准备数据
//
//        //商品分类名称
//        List<String> names = this.categoryClient.queryNameByIds(Arrays.asList(spu.getCid1(),spu.getCid2(),spu.getCid3()));
//        String all = spu.getTitle()+" "+StringUtils.join(names," ");
//
//        //sku集合
//        List<Sku> skus = this.goodsClient.querySkuBySpuId(id);
//        //处理sku
//        //把商品价格取出单独存放，便于展示
//        List<Long> prices = new ArrayList<>();
//        List<Map<String,Object>> skuList = new ArrayList<>();
//
//
//        for (Sku sku : skus) {
//            prices.add(sku.getPrice());
//            Map<String,Object> skuMap = new HashMap<>();
//            skuMap.put("id",sku.getId());
//            skuMap.put("title",sku.getTitle());
//            skuMap.put("image", StringUtils.isBlank(sku.getImages()) ? "":sku.getImages().split(",")[0]);
//            skuMap.put("price",sku.getPrice());
//            skuList.add(skuMap);
//        }
//
//        //spuDetail
//        SpuDetail spuDetail = this.goodsClient.querySpuDetailById(id);
//        //查询分类对应的规格参数
//        List<SpecParam> params = this.specificationClient.querySpecParam(null, spu.getCid3(), true, null);
//
//        //通用规格参数值
//        Map<Long, String> genericMap =
//                JsonUtils.parseMap(spuDetail.getGenericSpec(), Long.class, String.class);
//        //{"1":"其它","2":"G9青春版（全网通版）","3":2016,"5":143,"6":"其它","7":"Android","8":"骁龙（Snapdragon)","9":"骁龙617（msm8952）","10":"八核","11":1.5,"14":5.2,"15":"1920*1080(FHD)","16":800,"17":1300,"18":3000}
//
//        //特有规格参数的值
//        Map<Long,List<String>> specialMap = JsonUtils.nativeRead(spuDetail.getSpecialSpec(), new TypeReference<Map<Long, List<String>>>() {
//        });
//        //{"4":["白色","金色","玫瑰金"],"12":["3GB"],"13":["16GB"]}
//
//        //处理规格参数显示问题，默认显示id+值，处理后显示id对应的名称+值
//        Map<String, Object> specs = new HashMap<>();
//
//        for (SpecParam param : params) {
//            //规格参数的编号id id：1 表示的是品牌，4颜色
//            Long paramId = param.getId();
//
//            //今后显示的名称
//            String name = param.getName();//品牌，机身颜色
//            //通用参数
//            Object value = null;
//            if (param.getGeneric()){//是sku通用属性
//                //通用参数
//                value = genericMap.get(paramId);
//                System.out.println(value.toString()+" "+param);
//                if (param.getNumeric()&&StringUtils.isNotBlank(param.getSegments())){//是数字类型参数
//                    //数值类型需要加分段
//                    value = this.chooseSegment(value.toString(),param);//null
//                }
//            }
//            else {//特有参数
//                value = specialMap.get(paramId);
//
//            }
//            if (null==value){
//                value="其他";
//            }
//            specs.put(name,value);
//        }
//
//        Goods goods = new Goods();
//        goods.setId(spu.getId());
//        //这里如果要加品牌，可以再写个BrandClient，根据id查品牌
//        goods.setAll(all);
//        goods.setSubTitle(spu.getSubTitle());
//        goods.setBrandId(spu.getBrandId());
//        goods.setCid1(spu.getCid1());
//        goods.setCid2(spu.getCid2());
//        goods.setCid3(spu.getCid3());
//        goods.setCreateTime(spu.getCreateTime());
//        goods.setPrice(prices);
//        goods.setSkus(JsonUtils.serialize(skuList));
//        goods.setSpecs(specs);
//
//        return goods;
    }

    private String chooseSegment(String value, SpecParam p) {
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if(segs.length == 2){
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if(val >= begin && val < end){
                if(segs.length == 1){
                    result = segs[0] + p.getUnit() + "以上";
                }else if(begin == 0){
                    result = segs[1] + p.getUnit() + "以下";
                }else{
                    result = segment + p.getUnit();//4.5  4-5英寸
                }
                break;
            }
        }
        return result;
    }

    public void createIndex(Long id) throws IOException {
        System.out.println("id:"+id);
        Spu spu = this.goodsClient.findSpuById(id);//404
        SpuBo spuBO = new SpuBo();
        BeanUtils.copyProperties(spu,spuBO);
        Goods goods = buildGoods(spuBO);
        //把goods对象保存到索引库
        this.goodsRepository.save(goods);
    }

    public void deleteIndex(Long id) {
        goodsRepository.deleteById(id);
    }
}
