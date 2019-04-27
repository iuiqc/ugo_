package com.ugo.search.client;

import com.ugo.item.api.SpecificationApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Author: qc
 * @Date: 2019-4-25 16:55
 */
@FeignClient("item-service")
public interface SpecificationClient extends SpecificationApi {
}
