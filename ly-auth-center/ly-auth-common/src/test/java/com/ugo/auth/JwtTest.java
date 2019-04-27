package com.ugo.auth;

import com.ugo.auth.entity.UserInfo;
import com.ugo.auth.utils.JwtUtils;
import com.ugo.auth.utils.RsaUtils;
import org.junit.Before;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;
/**
 * @Author: qc
 * @Date: 2019/4/27 9:45
 */
public class JwtTest {

    private static final String pubKeyPath = "D:/heima/rsa/rsa.pub";

    private static final String priKeyPath = "D:/heima/rsa/rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "234");
    }

    @Before
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    @Test
    public void testGenerateToken() throws Exception {
        // 生成token
        String token = JwtUtils.generateToken(new UserInfo(20L, "jack"), privateKey, 5);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6MzIsInVzZXJuYW1lIjoiampqIiwiZXhwIjoxNTM2MjMwOTQyfQ.eq1AbdttZPgSA6ivUflITNtO92PAZLWBpnbOhXSHS1fX76V5Z6g9XbjuhCDL3vNBbcRajcxAWYrniaVGEOlguI2XR4AheVJ3Vi6JoxURRyOIp-2DXyEPW9n05S_4f3sLXYqD_nTkpUYTj26MwOcIzTfnzrUHZR0q2BKzRC-ZtI8";

        // 解析token
        UserInfo user = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + user.getId());
        System.out.println("userName: " + user.getUsername());
    }
}
