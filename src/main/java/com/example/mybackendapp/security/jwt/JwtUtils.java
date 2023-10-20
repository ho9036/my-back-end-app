package com.example.mybackendapp.security.jwt;

import com.example.mybackendapp.entity.UserPermission;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.util.Date;
import java.util.List;
import java.util.Objects;

public class JwtUtils {
    private static final int ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 2; // 30분

    public static final int REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24 * 15; // 15일

    private static final String key = "5958bc44-5463-441c-8a92-862b10f73828-b5bf40ad-ef91-44ee-93a1-a5201371bdcd" +
            "a3bfdfac-ba59-422a-93a1-84fb8b1e7b31-81f0cdae-1786-46f4-bba5-92010f7d2a14";

    private static final String EMAIL_KEY = "email";

    private static final String TOKEN_TYPE_KEY = "token_type";

    private static final String PERMISSION_KEY = "permission";

    // JWT Access Token 발급
    public static String createAccessToken(String email, List<UserPermission> permissions) {

        // Claim = Jwt Token에 들어갈 정보
        // Claim에 loginId를 넣어 줌으로써 나중에 loginId를 꺼낼 수 있음
        Claims claims = Jwts.claims();
        claims.put(EMAIL_KEY, email);
        claims.put(TOKEN_TYPE_KEY, "access_token");
        claims.put(PERMISSION_KEY, permissions.stream().map(UserPermission::getPermission).toList());

        Date now = new Date(System.currentTimeMillis());

        return Jwts.builder()
                .setSubject(email)
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + ACCESS_TOKEN_EXPIRE_TIME))
                .signWith(Keys.hmacShaKeyFor(key.getBytes()))
                .compact();
    }

    // JWT Refresh Token 발급
    public static String createRefreshToken(String email, List<UserPermission> permissions) {

        Claims claims = Jwts.claims();
        claims.put(EMAIL_KEY, email);
        claims.put(TOKEN_TYPE_KEY, "refresh_token");
        claims.put(PERMISSION_KEY, permissions.stream().map(UserPermission::getPermission).toList());

        Date now = new Date(System.currentTimeMillis());

        return Jwts.builder()
                .setSubject(email)
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + REFRESH_TOKEN_EXPIRE_TIME))
                .signWith(Keys.hmacShaKeyFor(key.getBytes()))
                .compact();
    }

    // Claims에서 email 꺼내기
    public static String getEmail(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(key.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get(EMAIL_KEY, String.class);
    }

    // Claims에서 permission 꺼내기
    public static List<String> getPermissions(String token) {
        List<?> permissions = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(key.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get(PERMISSION_KEY, List.class);

        if(permissions.stream().allMatch(item -> item instanceof String)){
            return permissions.stream().map(item -> (String) item).toList();
        }else{
            throw new RuntimeException("Permission is not String list");
        }
    }

    // Claims에서 refresh token인지 확인
    public static Boolean isRefreshToken(String token) {
        return Objects.equals(Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(key.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get(TOKEN_TYPE_KEY, String.class), "refresh_token");
    }

    // 밝급된 Token이 만료 시간이 지났는지 체크
    public static boolean isExpired(String token) {
        try{
            Date expireDate = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(key.getBytes()))
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration();

            return expireDate.before(new Date(System.currentTimeMillis()));
        }catch (Exception e) {
            return true;
        }
    }
}
