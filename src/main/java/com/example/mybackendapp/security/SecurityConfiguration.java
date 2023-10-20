package com.example.mybackendapp.security;

import com.example.mybackendapp.security.filter.JwtFilter;
import com.example.mybackendapp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfiguration{

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{

        // configuration
        http
                // Cross-Site Request Forgery 웹사이트에 로그인한 사용자가 의도하지 않은 요청을 통해 공격하는 것을 방지, 토큰 이용
                // (예, A라는 사이트에 로그인 했는데 같은 클라이언트의 B라는 사이트에서 A에 로그인 한 정보로 request 공격을 날리는 경우에 사용)
                // 현재는 사용 안할 거라 비활성화.
                .csrf(AbstractHttpConfigurer::disable)
                // Cross-Origin Resource Sharing 다른 도메인에서 자원을 공유할 수 있도록 허용하는 것
                // 허용 가능한 도메인을 수동으로 등록해야한다. 하지만 아무나 사용 할 수 있게 비활성화.
                .cors(AbstractHttpConfigurer::disable)
                // 인증 필터를 설정하는데, UsernamePasswordAuthenticationFilter 보다 선행되서 진행 할 필터를 추가함.
                // 추가하는 필터는 JWT 필터임.
                .addFilterBefore(new JwtFilter(), UsernamePasswordAuthenticationFilter.class)
                // http 요청에 대한 접근 제어 구성
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/user/create", "/api/auth/login",
                                "/api/auth/get-new-access-token").permitAll()
                        // 그 이외의 api는 인증된 사용자만 접근 가능
                        .anyRequest().authenticated())
                // 세션 관리 정책을 결정함.
                // 현재는 세션을 아예 비활성화 시킴. (JWT를 사용할 것이기 때문)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}


