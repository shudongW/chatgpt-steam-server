package com.tech.chatgpt.handler;

import com.tech.chatgpt.utils.TokenGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@Component
@Slf4j
public class MyInterceptor implements HandlerInterceptor {

    // 在请求处理之前进行拦截处理
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

//        System.out.println("preHandle");
        // 校验请求的token
        String token = request.getHeader("x-token");
        if (token == null || token.isEmpty()) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }
        if(!TokenGenerator.verifyToken(token)){
            // token校验失败,过期
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }

        return true;
    }

    // 在请求处理之后进行拦截处理
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // do nothing
    }

    // 在请求完成之后进行拦截处理
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // do nothing
    }
}
