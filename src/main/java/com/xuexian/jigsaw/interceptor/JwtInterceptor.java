package com.xuexian.jigsaw.interceptor;

import com.xuexian.jigsaw.dto.UserDTO;
import com.xuexian.jigsaw.util.JwtUtil;
import com.xuexian.jigsaw.util.UserHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        System.out.println("JWT拦截器 path=" + request.getRequestURI());
        String token = request.getHeader("Authorization");
        if (token == null ) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("缺少token");
            return false;
        }

        try {

            var claims = JwtUtil.parseToken(token);

            UserDTO user = new UserDTO(
                    Long.valueOf(claims.getId()),
                    claims.getSubject(),
                    claims.get("roles", List.class)
            );
            UserHolder.saveUser(user);
            return true;
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("token无效或过期");
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        UserHolder.removeUser();
    }
}
