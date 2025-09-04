package com.example.Loark.Config;

import com.example.Loark.Security.JwtAuthFilter;
import com.example.Loark.Security.OAuth2SuccessHandler;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
// ✅ 아래 2개 import 문을 추가해주세요.
import org.springframework.security.oauth2.client.web.CookieBasedOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // 1) CSRF 비활성, CORS 기본 설정
        http.csrf(csrf -> csrf.disable());
        http.cors(Customizer.withDefaults());

        // 2) 세션 완전 비활성 (STATELESS)
        http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // 3) 인증 실패 시 401 응답
        AuthenticationEntryPoint entryPoint = (req, res, ex) -> {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write("{\"error\":\"UNAUTHORIZED\",\"message\":\"로그인이 필요합니다.\"}");
        };
        http.exceptionHandling(eh -> eh.authenticationEntryPoint(entryPoint));

        // 4) OAuth2 로그인 설정
        http.oauth2Login(oauth -> oauth
                // ✅ 이 부분이 핵심 변경 사항입니다.
                // STATELESS 환경에서 OAuth2 state와 redirect_uri를 쿠키에 저장하기 위한 설정
                .authorizationEndpoint(endpoint -> endpoint
                        .authorizationRequestRepository(new CookieBasedOAuth2AuthorizationRequestRepository())
                )
                .successHandler(oAuth2SuccessHandler)
        );

        // 5) JWT 인증 필터 등록
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        // 6) 접근 제어
        http.authorizeHttpRequests(auth -> auth
                // 프론트 정적 파일/헬스체크/문서 등 허용
                .requestMatchers("/", "/index.html", "/favicon.ico", "/static/**").permitAll()
                .requestMatchers("/actuator/health", "/health").permitAll()

                // OAuth2 엔드포인트 및 인증 유틸은 허용
                .requestMatchers("/oauth2/**", "/login/**").permitAll()
                .requestMatchers("/auth/**").permitAll()

                // 공개 공대 목록은 비로그인 허용
                .requestMatchers(HttpMethod.GET, "/api/parties/public").permitAll()

                // 실제 API는 인증 필요
                .requestMatchers("/api/**").authenticated()

                // 그 외는 허용
                .anyRequest().permitAll()
        );

        return http.build();
    }
}