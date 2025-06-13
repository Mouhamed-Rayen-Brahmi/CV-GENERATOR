package com.example.backend.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class RequestInterceptor implements HandlerInterceptor {

    @Override
    public void postHandle(HttpServletRequest request,
                           jakarta.servlet.http.HttpServletResponse response,
                           Object handler,
                           ModelAndView modelAndView) throws Exception {

        if (modelAndView != null) {
            modelAndView.addObject("httpServletRequest", request);
        }
    }
}