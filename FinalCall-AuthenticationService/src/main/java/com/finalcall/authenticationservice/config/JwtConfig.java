// src/main/java/com/finalcall/authenticationservice/config/JwtConfig.java

package com.finalcall.authenticationservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

@Configuration
public class JwtConfig {

    @Value("${jwt.private.key.path}")
    private String privateKeyPath;

    @Bean
    public PrivateKey privateKey() throws Exception {
        String key = new String(Files.readAllBytes(Paths.get(privateKeyPath)));
        key = key.replaceAll("\\n", "")
                 .replace("-----BEGIN PRIVATE KEY-----", "")
                 .replace("-----END PRIVATE KEY-----", "");
        byte[] decoded = Base64.getDecoder().decode(key);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }
}
