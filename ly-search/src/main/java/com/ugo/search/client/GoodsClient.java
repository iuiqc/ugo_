package com.ugo.search.client;

import com.ugo.item.api.GoodsApi;
import com.ugo.item.pojo.Spu;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @Author: qc
 * @Date: 2019/4/23 13:27
 */
@FeignClient("item-service")
public interface GoodsClient extends GoodsApi {
}
