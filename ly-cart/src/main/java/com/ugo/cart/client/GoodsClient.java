package com.ugo.cart.client;

import com.ugo.item.api.GoodsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Author: qc
 * @Date: 2018-9-7 18:00
 */
@FeignClient("item-service")
public interface GoodsClient extends GoodsApi {
}
