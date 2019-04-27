package com.ugo.client;

import com.ugo.user.api.UserApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Author: qc
 * @Date: 2018-9-6 18:00
 */
@FeignClient("user-service")
public interface UserClient extends UserApi{
}
