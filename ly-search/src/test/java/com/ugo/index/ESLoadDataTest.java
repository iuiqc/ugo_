package com.ugo.index;

import com.ugo.LySearchService;
import com.ugo.common.pojo.PageResult;
import com.ugo.item.bo.SpuBo;
import com.ugo.search.client.GoodsClient;
import com.ugo.search.pojo.Goods;
import com.ugo.search.repository.GoodsRepository;
import com.ugo.search.service.IndexService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LySearchService.class)
public class ESLoadDataTest {

    @Autowired
    private IndexService indexService;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private GoodsRepository goodsRepository;
    @Test
    public void loadData(){
        int page = 1;
        int rows = 100;
        int size = 0;
        do {
            // 查询spu
            PageResult<SpuBo> result = this.goodsClient.querySpuByPage(page, rows, true, null);

            List<SpuBo> spus = result.getItems();


            // spu转为goods
            List<Goods> goods = spus.stream().map(spu -> {
                try {
                    return this.indexService.buildGoods(spu);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            })
                    .collect(Collectors.toList());

            // 把goods放入索引库
            this.goodsRepository.saveAll(goods);

            size = spus.size();
            page++;
        }while (size == 100);
        System.out.println("loadData_200");
    }
}
