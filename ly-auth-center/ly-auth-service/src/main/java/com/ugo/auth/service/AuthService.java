package com.ugo.auth.service;

import com.ugo.auth.config.JwtProperties;
import com.ugo.auth.entity.UserInfo;
import com.ugo.auth.utils.JwtUtils;
import com.ugo.client.UserClient;
import com.ugo.user.pojo.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import java.security.PrivateKey;

/**
 * @Author: qc
 * @Date: 2019/4/24 13:29
 */
@Service
@EnableConfigurationProperties(JwtProperties.class)
public class AuthService {
    @Autowired
    private UserClient userClient;

    @Autowired
    private JwtProperties jwtProperties;
    /**
     * 查询数据库
     */
    Logger logger = LoggerFactory.getLogger(AuthService.class);
    public String authentication(String username, String password) {
        try {
            User user = userClient.queryUser(username, password);
            if (null==user){
                logger.error("用户验证失败");
                return null;
            }
            //载荷信息
            UserInfo userInfo = new UserInfo();
            userInfo.setId(user.getId());
            userInfo.setUsername(user.getUsername());
            //生成token
            String token = JwtUtils.generateToken(userInfo,jwtProperties.getPrivateKey(),jwtProperties.getExpire());
            return token;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
