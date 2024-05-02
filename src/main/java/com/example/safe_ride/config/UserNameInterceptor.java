package com.example.safe_ride.config;

import com.example.safe_ride.facade.AuthenticationFacade;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class UserNameInterceptor implements HandlerInterceptor {
    private final AuthenticationFacade authFacade;

    public UserNameInterceptor(AuthenticationFacade authFacade) {
        this.authFacade = authFacade;
    }

    // 컨트롤러가 호출되기 전 모든 요청에 사용자 이름을 전달
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Authentication authentication = authFacade.getAuth();
        if (authentication != null && authentication.isAuthenticated()) {
            String userName = authentication.getName();
            request.setAttribute("userName", userName);
        }
        return true;
    }
}
