package com.example.safe_ride.locationInfo.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class PublicBicycleInterceptor implements HandlerInterceptor {
    private final TempTableService tempTableService;
    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex
    ) throws Exception {
        HttpSession session = request.getSession(false);
        if (session != null && !request.getRequestURI().startsWith("/public-bicycle/")) {
            session.removeAttribute("isSearched");
            session.removeAttribute("infoResponse");
        }
        tempTableService.clearData();
    }
}