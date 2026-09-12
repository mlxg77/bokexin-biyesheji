package com.silk.controller;

import com.silk.entity.User;
import com.silk.framework.jwt.JwtUtil;
import com.silk.service.UserService;
import com.silk.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author LindaSilk
 * @date 2021年3月09日, 周二
 */
@RestController
public class LoginController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    //  从前端传入的JSON中获取对象信息
    public Result login(@RequestBody User user){

        User entity = userService.login(user.getUserName(), user.getUserPwd(), user.getUserType());
        if (entity != null && entity.getUserType() != null && entity.getUserType() == 3){
            // 禁用账号即使用user_type=3匹配到也不发token，防止绕过前端直接调接口登录
            return Result.fail("该账号已被禁用，如有疑问请联系管理员");
        }
        if (entity != null){
            String token = JwtUtil.sign(entity);
            Map map = new HashMap();
            map.put(JwtUtil.token, token);
            map.put("user", entity);
            return Result.ok("登录成功", map);
        }else {
            return Result.fail("用户名或密码或角色错误");
        }
    }

    @PostMapping("/register")
    //  注册账号：只需账号和密码，默认注册为学生身份
    public Result register(@RequestBody User user){

        if (user.getUserName() == null || user.getUserName().trim().isEmpty()
                || user.getUserPwd() == null || user.getUserPwd().trim().isEmpty()){
            return Result.fail("请把账号和密码都填写完整哦");
        }

        int flag = userService.register(user);
        if (flag > 0){
            return Result.ok("注册成功，请使用学生身份登录");
        }else {
            return Result.fail("该账号已经被注册啦，换一个试试吧");
        }
    }
}
