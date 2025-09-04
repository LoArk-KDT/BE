package com.example.Loark.Config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import java.time.Duration;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        // HTTP 요청을 만드는 Factory 객체를 생성합니다.
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();

        // ✅ 여기에 타임아웃을 직접 설정합니다. (Deprecated 아님)
        factory.setConnectTimeout(Duration.ofSeconds(30)); // 연결 타임아웃: 30초
        factory.setReadTimeout(Duration.ofMinutes(1));     // 읽기 타임아웃: 1분

        // 설정이 적용된 Factory를 사용하여 RestTemplate을 생성합니다.
        return new RestTemplate(factory);
    }
}