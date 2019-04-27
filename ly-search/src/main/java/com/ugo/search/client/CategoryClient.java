package com.ugo.search.client;

import com.ugo.item.api.CategoryApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Author: qc
 * @Date: 2019-4-25 16:20
 */
@FeignClient("item-service")
public interface CategoryClient extends CategoryApi {

}
