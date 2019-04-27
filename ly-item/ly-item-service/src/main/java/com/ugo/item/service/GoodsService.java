package com.ugo.item.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.ugo.common.pojo.PageResult;
import com.ugo.item.bo.SpuBo;
import com.ugo.item.controller.GoodsController;
import com.ugo.item.mapper.*;
import com.ugo.item.pojo.Sku;
import com.ugo.item.pojo.Spu;
import com.ugo.item.pojo.SpuDetail;
import com.ugo.item.pojo.Stock;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: qc
 * @Date: 2019-4-22 17:53
 */
@Slf4j
@Service
public class GoodsService {

    //首先查询的一定是spu表
    //得到的结果，有两个值是不能直接使用（cid，bid）
    //需要把查询到的spu转为spobo，
    //根据spu中记录的category的信息去查分类的名称
    //根据spu中记录brand的信息去查询品牌名称

    @Autowired
    private BrandMapper brandMapper;

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SpuDetailMapper spuDetailMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    private Logger logger = LoggerFactory.getLogger(GoodsService.class);

    public PageResult<SpuBo> queryGoods(Integer page, Integer rows, Boolean saleable, String key) {

        //开启分页

        PageHelper.startPage(page,rows);

        //构建查询条件
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();

        if (null!=saleable){
            criteria.andEqualTo("saleable",saleable);
        }

        if (StringUtils.isNotBlank(key)){
            criteria.andLike("title","%"+key+"%");
        }

        Page<Spu> pageInfo = (Page<Spu>) spuMapper.selectByExample(example);

        //把这其中得spu变成spubo

        List<SpuBo> list = new ArrayList<>();

        //以下操作就是把spu转为SpuBo

        for (Spu spu : pageInfo.getResult()) {
            SpuBo spuBO = new SpuBo();

            //把spu中的所有值赋值给spuBO
            BeanUtils.copyProperties(spu,spuBO);
            //System.out.println(spu.getBrandId()+" "+brandMapper.selectByPrimaryKey(spu.getBrandId()).getName());
            //把spu中的品牌id取出，执行查询，把查到的品牌对象的名称赋值给spuBO
            try {
                spuBO.setBname(brandMapper.selectByPrimaryKey(spu.getBrandId()).getName());//nullpoint品牌删除导致的空指针异常
            }catch (Exception e)
            {
                log.error("品牌缺失");
            }

            List<String> names = categoryService.queryCategoryNameByCids(Arrays.asList(spu.getCid1(),spu.getCid2(),spu.getCid3()));

            String join = StringUtils.join(names, "/");

            spuBO.setCname(join);

            list.add(spuBO);

        }

        return new PageResult<>(pageInfo.getTotal(),list);
    }


    @Transactional
    public void saveGoods(SpuBo spuBO) {

        // 保存spu
        spuBO.setSaleable(true);
        spuBO.setValid(true);
        spuBO.setCreateTime(new Date());
        spuBO.setLastUpdateTime(spuBO.getCreateTime());
        this.spuMapper.insert(spuBO);

        //保存spuDetail信息
        spuBO.getSpuDetail().setSpuId(spuBO.getId());

        spuDetailMapper.insert(spuBO.getSpuDetail());

        // 保存sku和库存信息
        saveSkuAndStock(spuBO.getSkus(), spuBO.getId());

        //发送消息mq
        sendMessage(spuBO.getId(),"insert");
    }

    private void saveSkuAndStock(List<Sku> skus, Long spuId) {

        for (Sku sku : skus) {
            if (!sku.getEnable()){
                continue;
            }
            //保存sku
            sku.setSpuId(spuId);
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());

            this.skuMapper.insert(sku);

            //保存库存信息
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());

            this.stockMapper.insert(stock);

        }
    }

    public List<Sku> querySkuBySpuId(Long id) {
        Sku sku = new Sku();
        sku.setSpuId(id);
        return skuMapper.select(sku);
    }

    public SpuDetail querySpuDetailById(Long id) {

        return spuDetailMapper.selectByPrimaryKey(id);
    }

    public Spu querySpuById(Long id) {
        return this.spuMapper.selectByPrimaryKey(id);
    }

    /**
     *
     * @param id
     * @param type  操作的类型
     */
    /**
     * @Author: qc
     * @Date: 2019/4/23 20:40
     * 送消息到mq
     */
    private void sendMessage(Long id, String type){
        // 发送消息
        try {
            this.amqpTemplate.convertAndSend("item." + type, id);
        } catch (Exception e) {
            logger.error("{}商品消息发送异常，商品id：{}", type, id, e);
        }
    }

    public Sku querySkuById(Long id) {
        return this.skuMapper.selectByPrimaryKey(id);
    }
    /**
     * @Author: qc
     * @Date: 2019/4/22 19:13
     */
    public void updateGoods(SpuBo spuBo) {
        /**
         * 更新策略：
         *      1.判断tb_spu_detail中的spec_template字段新旧是否一致
         *      2.如果一致说明修改的只是库存、价格和是否启用，那么就使用update
         *      3.如果不一致，说明修改了特有属性，那么需要把原来的sku全部删除，
         *      然后添加新的sku
         */

        //更新spu
        spuBo.setSaleable(true);
        spuBo.setValid(true);
        spuBo.setLastUpdateTime(new Date());
        this.spuMapper.updateByPrimaryKeySelective(spuBo);

        //更新spu详情
        SpuDetail spuDetail = spuBo.getSpuDetail();
        String oldTemp = this.spuDetailMapper.selectByPrimaryKey(spuBo.getId()).getSpecialSpec();
        if (spuDetail.getSpecialSpec().equals(oldTemp)){
            //相等，sku update
            //更新sku和库存信息
            updateSkuAndStock(spuBo.getSkus(),spuBo.getId(),true);
        }else {
            //不等，sku insert
            //更新sku和库存信息
            updateSkuAndStock(spuBo.getSkus(),spuBo.getId(),false);
        }
        spuDetail.setSpuId(spuBo.getId());
        this.spuDetailMapper.updateByPrimaryKeySelective(spuDetail);

        //发送消息到mq
        this.sendMessage(spuBo.getId(),"update");
    }
    /**
     * @Author: qc
     * @Date: 2019/4/22 21:06
     */
    private void updateSkuAndStock(List<Sku> skus,Long id,boolean tag) {
        //通过tag判断是insert还是update
        //获取当前数据库中spu_id = id的sku信息
        Example e = new Example(Sku.class);
        e.createCriteria().andEqualTo("spuId",id);
        //oldList中保存数据库中spu_id = id 的全部sku
        List<Sku> oldList = this.skuMapper.selectByExample(e);
        if (tag){
            /**
             * 判断是更新时是否有新的sku被添加：如果对已有数据更新的话，则此时oldList中的数据和skus中的ownSpec是相同的，否则则需要新增
             */
            int count = 0;
            for (Sku sku : skus){
                if (!sku.getEnable()){
                    continue;
                }
                for (Sku old : oldList){
                    if (sku.getOwnSpec().equals(old.getOwnSpec())){
                        System.out.println("更新");
                        //更新
                        List<Sku> list = this.skuMapper.select(old);
                        if (sku.getPrice() == null){
                            sku.setPrice(0L);
                        }
                        if (sku.getStock() == null){
                            sku.setStock(0);
                        }
                        sku.setId(list.get(0).getId());
                        sku.setCreateTime(list.get(0).getCreateTime());
                        sku.setSpuId(list.get(0).getSpuId());
                        sku.setLastUpdateTime(new Date());
                        this.skuMapper.updateByPrimaryKey(sku);
                        //更新库存信息
                        Stock stock = new Stock();
                        stock.setSkuId(sku.getId());
                        stock.setStock(sku.getStock());
                        this.stockMapper.updateByPrimaryKeySelective(stock);
                        //从oldList中将更新完的数据删除
                        oldList.remove(old);
                        break;
                    }else{
                        //新增
                        count ++ ;
                    }
                }
                if (count == oldList.size() && count != 0){
                    //当只有一个sku时，更新完因为从oldList中将其移除，所以长度变为0，所以要需要加不为0的条件
                    List<Sku> addSku = new ArrayList<>();
                    addSku.add(sku);
                    saveSkuAndStock(addSku,id);
                    count = 0;
                }else {
                    count =0;
                }
            }
            //处理脏数据
            if (oldList.size() != 0){
                for (Sku sku : oldList){
                    this.skuMapper.deleteByPrimaryKey(sku.getId());
                    Example example = new Example(Stock.class);
                    example.createCriteria().andEqualTo("skuId",sku.getId());
                    this.stockMapper.deleteByExample(example);
                }
            }
        }else {
            List<Long> ids = oldList.stream().map(Sku::getId).collect(Collectors.toList());
            //删除以前的库存
            Example example = new Example(Stock.class);
            example.createCriteria().andIn("skuId",ids);
            this.stockMapper.deleteByExample(example);
            //删除以前的sku
            Example example1 = new Example(Sku.class);
            example1.createCriteria().andEqualTo("spuId",id);
            this.skuMapper.deleteByExample(example1);
            //新增sku和库存
            saveSkuAndStock(skus,id);
        }


    }
}
