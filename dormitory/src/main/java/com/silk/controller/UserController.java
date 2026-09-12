package com.silk.controller;

import com.github.pagehelper.PageInfo;
import com.silk.entity.User;
import com.silk.framework.exception.MyException;
import com.silk.service.UserService;
import com.silk.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;


/**
 * @author LindaSilk
 * @date 2021年3月03日, 周三
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("create")
    public Result create(@RequestBody User user){       // 映射到Json对象
        int flag = userService.create(user);
        if(flag>0){
            return Result.ok();
        }else{
            return Result.fail();
        }
    }

    @GetMapping("delete")
    public Result delete(String ids){
        int flag = userService.delete(ids);
        if(flag>0){
            return Result.ok();
        }else{
            return Result.fail();
        }
    }

    @PostMapping("update")
    public Result update(@RequestBody User user){
        int flag = userService.updateSelective(user);       // 选择性修改
        if(flag>0){
            return Result.ok();
        }else{
            return Result.fail();
        }
    }

    @GetMapping("detail")
    public User detail(Integer id){
        return userService.detail(id);
    }

    @PostMapping("query")
    public Map<String,Object> query(@RequestBody User user){            // @RequestBody:将json格式的数据转为java对象
        PageInfo<User> pageInfo = userService.query(user);
        return Result.ok(pageInfo);
    }

    @GetMapping("disable")
    public Result disable(String ids, HttpServletRequest request){     // 禁用账号：user_type置为3
        User operator = (User) request.getAttribute("user");          // 获取发起当前操作的用户信息
        try {
            int flag = userService.disable(ids, operator);
            if(flag > 0){
                return Result.ok("已禁用" + flag + "个账号");
            }else{
                return Result.fail("没有可禁用的账号，请先勾选正常状态的账号");
            }
        } catch (MyException e){                                        // 校验不通过时返回失败，便于前端区分
            return Result.fail(e.getMessage());
        }
    }

    @PostMapping("enable")
    public Result enable(@RequestBody User user){                      // 启用账号：恢复为学生或宿管员
        try {
            int flag = userService.enable(user.getId(), user.getUserType());
            if(flag > 0){
                return Result.ok("账号已恢复使用");
            }else{
                return Result.fail();
            }
        } catch (MyException e){
            return Result.fail(e.getMessage());
        }
    }

}
