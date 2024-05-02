package com.example.safe_ride.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;


@Configuration
@RequiredArgsConstructor
public class WebSecurityConfig {
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                    // 모든 접근 허가
                    .requestMatchers(
                            "/safe-ride",
                            "/safe-ride/login",
                            "/safe-ride/join",

                            //템플릿 관련
                            "/css/**",
                            "/js/**",
                            "/fonts/**",
                            "/img/**",

                            //회원가입 관련
                            "/safe-ride/duplicateCkForId",
                            "/safe-ride/duplicateCkForNickname",
                            "/safe-ride/duplicateCkForEmail",

                            // 공영자전거 관련
                            "/public-bicycle/**"

                    )
                    .permitAll()

                    //인증된 사용자 접근 허가
                    .requestMatchers(
                            "/safe-ride/logout",
                            "/safe-ride/myprofile",
                            "/safe-ride/myprofile/update",
                            "/safe-ride/myprofile/delete",
                            "/safe-ride/myprofile/create-today"
                    ).authenticated()

                    .requestMatchers(
                            "/route",
                            "/safe-ride/weather/**",
                            "/safety-direction/**"
                    )
                    .authenticated()

                    // 커뮤니티 - 게시글
                    .requestMatchers(
                            "/article",
                            "/article/create",
                            "/article/filter",
                            "/article/{id}",
                            "/article/{id}/edit",
                            "/article/{id}/delete",
                            "/city",
                            "/article/{articleId}/comment",
                            "/article/{articleId}/comment/{commentId}/delete"
                    ).authenticated()

                    // 커뮤니티 - 매칭글
                    .requestMatchers(
                            "/matching/list",
                            "/matching/create",
                            "/matching/{id}",
                            "/matching/{id}/edit",
                            "/matching/{id}/delete",
                            "/matching/{id}/apply",
                            "/matching/{matchingId}/accept/{applicationId}",
                            "/matching/{matchingId}/reject/{applicationId}",
                            "/matching/{matchingId}/cancel-application",
                            "/matching/{matchingId}/manner",
                            "/matching/{id}/end"
                    ).authenticated()
            )
            // html form 요소를 이용해 로그인을 시키는 설정
            .formLogin(formLogin -> formLogin
                    // 어떤 경로(URL)로 요청을 보내면
                    // 로그인 페이지가 나오는지
                    .loginPage("/safe-ride/login")
                    //로그인에 성공한 뒤 이동할 디폴트 URL
                    .defaultSuccessUrl("/safe-ride")
                    //로그인 진행 url
                    .loginProcessingUrl("/safe-ride/login")
                    //로그인 성공했을 때 작업
                    .successHandler(new AuthenticationSuccessHandler() {
                      @Override
                      public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
                        //인증성공시
                        response.sendRedirect("/safe-ride");
                      }
                    })
                    // 실패시 이동할 URL
                    .failureUrl("/safe-ride/login?fail").permitAll())
            //로그아웃처리
            .logout(logout -> logout
                    //로그아웃 url
                    .logoutUrl("/safe-ride/logout")
                    //로그아웃 성공 시 이동할 url
                    .logoutSuccessUrl("/safe-ride").invalidateHttpSession(true)// http 세션 무효화 여부
                    .deleteCookies("JSESSIONID") //해당 쿠키 삭제
                    .permitAll()
            );
    return http.build();
  }
}