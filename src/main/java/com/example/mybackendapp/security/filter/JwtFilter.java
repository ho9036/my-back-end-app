package com.example.mybackendapp.security.filter;

import com.example.mybackendapp.security.PermissionAuthority;
import com.example.mybackendapp.security.jwt.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        // Header의 Authorization 값이 비어있으면 => Jwt Token을 전송하지 않음 => 로그인 하지 않음
        if(authorizationHeader == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // Header의 Authorization 값이 'Bearer '로 시작하지 않으면 => 잘못된 토큰
        if(!authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 전송받은 값에서 'Bearer ' 뒷부분(Jwt Token) 추출
        String token = authorizationHeader.split(" ")[1];

        // 전송받은 Jwt Token이 만료되었으면 => 다음 필터 진행(인증 X)
        if(JwtUtils.isExpired(token)) {

            filterChain.doFilter(request, response);
            return;
        }

        if(JwtUtils.isRefreshToken(token)){
            boolean isAllowRequestUrl = request.getRequestURL().toString().endsWith("/api/auth/get-new-access-token");
            if(!isAllowRequestUrl){
                filterChain.doFilter(request, response);
                return;
            }
        }

        try {
            // Jwt에서 email 추출
            String email = JwtUtils.getEmail(token);

            List<String> permissions = JwtUtils.getPermissions(token);
            // email 정보로 UsernamePasswordAuthenticationToken 발급
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    email, null, permissions.stream().map(PermissionAuthority::new).toList());
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            // 권한 부여
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        } catch (Exception ignored) {

        } finally {
            filterChain.doFilter(request, response);
        }
    }
}
