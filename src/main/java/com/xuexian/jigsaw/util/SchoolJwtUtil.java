package com.xuexian.jigsaw.util;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SecureDigestAlgorithm;
import lombok.SneakyThrows;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SchoolJwtUtil {
    //jwt加密方式
    private static final SecureDigestAlgorithm<SecretKey,SecretKey> ALGORITHM = Jwts.SIG.HS256;

    private static final String CLAIM_KEY = "casID";

    // 十秒过期
    private static final long expire = 10000;

    private static final byte[] salt = "KOISHIKISHIKAWAIIKAWAIIKISSKISSLOVELY".getBytes(StandardCharsets.UTF_8);

    private static final int iterationCount = 114514;

    private static final Map<String, SecretKey> KEY_CACHE = new ConcurrentHashMap<>();

    @SneakyThrows
    private static SecretKey generateSecretKey(String key) {
        SecretKey secretKey = KEY_CACHE.get(key);
        if (secretKey == null) {
            PBEKeySpec spec = new PBEKeySpec(key.toCharArray(), salt, iterationCount, 256);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] secretBytes = factory.generateSecret(spec).getEncoded();
            secretKey = new SecretKeySpec(secretBytes, "HmacSHA256");
            KEY_CACHE.put(key, secretKey);
        }
        return secretKey;
    }

    public static String generate(String obj, String key) {
        SecretKey secretKey = generateSecretKey(key);
        return Jwts.builder()
                .header().add("type","JWT")
                .and()
                .claim(CLAIM_KEY, obj)
                .expiration(new Date(System.currentTimeMillis() + expire))
                .signWith(secretKey,ALGORITHM)
                .compact();
    }

    //解析token，得到包装的casId
    public static String getClaim(String token, String key){
        SecretKey secretKey = generateSecretKey(key);
        try {
            return (String) Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .get(CLAIM_KEY);
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }
}
