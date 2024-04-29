package com.example.safe_ride.config;

import com.example.safe_ride.locationInfo.service.PublicBicycleInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.Formatter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    private final UserNameInterceptor userNameInterceptor;
    private final PublicBicycleInterceptor publicBicycleInterceptor;

    public WebMvcConfig(
            UserNameInterceptor userNameInterceptor,
            PublicBicycleInterceptor publicBicycleInterceptor
    ) {
        this.userNameInterceptor = userNameInterceptor;
        this.publicBicycleInterceptor = publicBicycleInterceptor;
    }

    @Bean
    public Formatter<LocalDateTime> localDateTimeFormatter() {
        return new Formatter<>() {
            @Override
            public LocalDateTime parse(String text, Locale locale) {
                return LocalDateTime.parse(text, DateTimeFormatter.ISO_DATE_TIME);
            }

            @Override
            public String print(LocalDateTime object, Locale locale) {
                return DateTimeFormatter.ofPattern("MM월 dd일 EEEE HH:mm", locale).format(object);
            }
        };
    }

    // 사용자 이름을 모델에 추가
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userNameInterceptor);
        // '/public-bicycle/**' 패턴에 대해 인터셉터 적용
        registry.addInterceptor(publicBicycleInterceptor)
                .addPathPatterns("/")
                .excludePathPatterns("/public-bicycle"); // 이 경로 제외
    }
}
