package com.atguigu.gmall.gateway.config;

import com.atguigu.gmall.common.utils.RsaUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * @author Lee
 * @date 2020-10-14  16:44
 */
@Data
@Slf4j
@Component
@ConfigurationProperties(prefix = "auth.jwt")
public class JwtProperties {


    private String pubKeyPath;
    private String cookieName;
    private String headerName;

    private PublicKey publicKey;

    // 该方法在构造方法执行之后执行
    @PostConstruct
    public void init(){
        try {
            this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        } catch (Exception e) {
            log.error("生成公钥和私钥出错");
            e.printStackTrace();
        }
    }
}
