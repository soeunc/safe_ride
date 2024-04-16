package com.example.safe_ride.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;


@Configuration
@RequiredArgsConstructor
public class WebSecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                        auth -> auth
                                // 모든 접근 허가
                                .requestMatchers(
                                        "/safe-ride",//메인페이지
                                        "/safe-ride/login",//로그인
                                        "/safe-ride/join",//회원가입
                                        //템플릿 관련
                                        "/css/**",
                                        "/js/**",
                                        "/fonts/**",
                                        "/img/**"
                                )
                                // 이 경로에 도달할 수 있는 사람에 대한 설정(모두)
                                .permitAll()
//                                //익명사용자 접근 허가
//                                .requestMatchers(
//                                    "/safe-ride/login",//로그인
//                                    "/safe-ride/join"//회원가입
//                                )
//                                .anonymous()
                                //인증된 사용자 접근 허가
                                .requestMatchers(
                                        "/safe-ride/logout",//로그아웃
                                        "/safe-ride/myprofile",//마이페이지
                                        "/safe-ride/myprofile/update",//회원정보 수정
                                        "/safe-ride/myprofile/delete",//회원정보 삭제
                                        "/safe-ride/myprofile/create-today"//오늘 기록 입력
                                )
                                .authenticated()

                )
                // html form 요소를 이용해 로그인을 시키는 설정
                .formLogin(
                        formLogin -> formLogin
                                // 어떤 경로(URL)로 요청을 보내면
                                // 로그인 페이지가 나오는지
                                .loginPage("/safe-ride/login")
                                //로그인에 성공한 뒤 이동할 디폴트 URL
                                .defaultSuccessUrl("/safe-ride")
                                //로그인 진행 url
                                .loginProcessingUrl(
                                        "/safe-ride/login"
                                )
                                //로그인 성공했을 때 작업
                                .successHandler(
                                        new AuthenticationSuccessHandler() {
                                            @Override
                                            public void onAuthenticationSuccess(
                                                    HttpServletRequest request,
                                                    HttpServletResponse response,
                                                    Authentication authentication
                                            ) throws IOException, ServletException {
                                                //System.out.println("authentication: "+authentication.getName());
                                                //인증성공시
                                                response.sendRedirect("/safe-ride");
                                            }
                                        }
                                )
                                // 실패시 이동할 URL
                                .failureUrl("/safe-ride/login?fail")
                                .permitAll()
                )
                //로그아웃처리
                .logout(
                        logout -> logout
                                //로그아웃 url
                                .logoutUrl("/safe-ride/logout")
                                //로그아웃 성공 시 이동할 url
                                .logoutSuccessUrl("/safe-ride")
                                .invalidateHttpSession(true)// http 세션 무효화 여부
                                .deleteCookies("JSESSIONID") //해당 쿠키 삭제
                                .permitAll()
                )
                   

        ;

        return http.build();
    }
}