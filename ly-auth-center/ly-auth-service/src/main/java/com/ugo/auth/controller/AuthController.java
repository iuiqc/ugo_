package com.ugo.auth.controller;

import com.ugo.auth.config.JwtProperties;
import com.ugo.auth.entity.UserInfo;
import com.ugo.auth.service.AuthService;
import com.ugo.auth.utils.JwtUtils;
import com.ugo.common.utils.CookieUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;

/**
 * @Author: qc
 * @Date: 2018-9-6 17:50
 */
@RestController
@EnableConfigurationProperties(JwtProperties.class)
public class AuthController {

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private AuthService authService;

    /**
     * 授权内容为token，校验内容为username，password，校验方式采用远程服务调用请求user-service查询，查到了封装token
     * <p>
     * token保存到cookie中
     *
     * @param username
     * @param password
     * @param request
     * @param response
     * @return
     */

    @PostMapping("accredit")
    public ResponseEntity<Void> accredit(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        // 登录校验,先去查询数据库
        String token = this.authService.authentication(username, password);
        if (null == token) {
            //没有认证
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);//401
        }

        //controller得到token要把token回送到客户端
        CookieUtils.setCookie(request, response, jwtProperties.getCookieName(),
                token, jwtProperties.getCookieMaxAge(), null, true);

        return ResponseEntity.ok().build();
    }

    /**
     * 刷新token
     *
     * @Author: qc
     * @Date: 2019/4/24 17:19
     */
    @GetMapping("verify")
    public ResponseEntity<UserInfo> verify(@CookieValue("LY_TOKEN") String token, HttpServletRequest request, HttpServletResponse response) {


        try {
            UserInfo user = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());

            //刷新token   用户预测  缓式写入  20min
            String newToken = JwtUtils.generateToken(user, jwtProperties.getPrivateKey(), jwtProperties.getExpire());

            CookieUtils.setCookie(request, response, jwtProperties.getCookieName(), newToken, jwtProperties.getCookieMaxAge(), null, true);
            if (null == user) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);//401
            }
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            e.printStackTrace();

        }
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

    }
}
