package com.atguigu.gmall.auth;

import com.atguigu.gmall.common.utils.JwtUtils;
import com.atguigu.gmall.common.utils.RsaUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

public class JwtTest {

    // 别忘了创建D:\\project\rsa目录
	private static final String pubKeyPath = "F:\\Java0422\\WorkSpace\\rsa\\rsa.pub";
    private static final String priKeyPath = "F:\\Java0422\\WorkSpace\\rsa\\rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "ar%hu@u54y5y11#");
    }

    @BeforeEach
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    @Test
    public void testGenerateToken() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("id", "11");
        map.put("username", "liuyan");
        // 生成token
        String token = JwtUtils.generateToken(map, privateKey, 5);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6IjExIiwidXNlcm5hbWUiOiJsaXV5YW4iLCJleHAiOjE2MDI2NjUxNTZ9.JAUWaY9537DEQg7aJ7JJpTd7ugu-vOyjk9LCyK8W7n7rJzE3e_CNBIa6ejJQcCDMMprYj8x7eEd_5E_36aXIGmBqUj7b_TD3nXF97bs0SnIEzGbXTLGlFeROI4Wz4jB6Dj7vYM3v2Avt2pQRGlIpTQ09WEA_zYAYUusYdFmApOnQIQmMqf73YtkRguwcgcYFEbW4TjqbpRqLKvrbQYE5rX_9WgBEO21HWAik_lfpNhPq8rL8cjjWqCHl9L-jgqyqCfPSMFPUpOywf84SJwd_dNMekVMTKDmCXWW-IUAtXGy9aUwbsuRq72KAce86gGKde3FGaJm55nCx635VooTMjg";

        // 解析token
        Map<String, Object> map = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + map.get("id"));
        System.out.println("userName: " + map.get("username"));
    }
}