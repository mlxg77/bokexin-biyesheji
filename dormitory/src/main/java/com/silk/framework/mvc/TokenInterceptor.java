package com.silk.framework.mvc;

import com.silk.entity.User;
import com.silk.framework.exception.MyException;
import com.silk.framework.jwt.JwtUtil;
import com.silk.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author LindaSilk
 * @date 2021年3月10日, 周三
 * @description Token拦截器
 */
public class TokenInterceptor implements HandlerInterceptor {

    @Autowired
    private UserMapper userMapper;             // 用于查询账号最新状态（token本身无法感知数据库变化）

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();

        // 静态资源请求直接放行，不做token校验
        if (isStaticResource(uri)) {
            System.out.println("[TokenInterceptor] 跳过静态资源: " + uri);
            return true;
        }

        System.out.println("[TokenInterceptor] 拦截请求: " + uri);

        String token = request.getHeader(JwtUtil.token);
        User user = JwtUtil.getUser(token);             // 根据token获取用户对象
        if (user == null) {
            throw new MyException("超时或不合法的Token！");
        }

        // 查库校验账号最新状态：被禁用(user_type=3)或已删除的账号立即失效，防止旧token滚动续期绕过禁用
        User latest = userMapper.detail(user.getId());
        if (latest == null || (latest.getUserType() != null && latest.getUserType() == 3)){
            // 直接写响应而不抛异常：避免被全局异常处理器包装成code=200，前端才能正确识别为失败
            response.setStatus(200);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":400,\"msg\":\"该账号已被禁用，如有疑问请联系管理员\"}");
            return false;
        }

        String newToken = JwtUtil.sign(latest);         // 生成一个新的token
        response.setHeader(JwtUtil.token, newToken);
        response.setHeader("Access-Control-Expose-Headers", JwtUtil.token); // 将token暴露出来(用于跨域)
        request.setAttribute("user", latest);        // 设置用户信息
        return true;
    }

    /**
     * 判断是否为静态资源请求
     */
    private boolean isStaticResource(String uri) {
        return uri.endsWith(".html") || uri.endsWith(".css") || uri.endsWith(".js")
                || uri.endsWith(".png") || uri.endsWith(".jpg") || uri.endsWith(".jpeg")
                || uri.endsWith(".gif") || uri.endsWith(".ico") || uri.endsWith(".svg")
                || uri.endsWith(".json") || uri.endsWith(".txt") || uri.endsWith(".woff")
                || uri.endsWith(".woff2") || uri.endsWith(".ttf") || uri.endsWith(".map");
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
