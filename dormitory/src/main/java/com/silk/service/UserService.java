package com.silk.service;

import com.silk.mapper.UserMapper;
import com.silk.entity.User;
import com.silk.framework.exception.MyException;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;


/**
 * @author LindaSilk
 * @date 2021年3月07日, 周日
 */
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    public int create(User user) {
        return userMapper.create(user);
    }

    public int delete(String ids) {
        String[] arr = ids.split(",");              // 将前端删除时传递的多个id组成的字符串分开
        int row = 0;
        for (String s : arr) {
            if(!StringUtils.isEmpty(s)){
                userMapper.delete(Integer.parseInt(s));
            row++;
            }
        }
        return row;
    }

    public int delete(Integer id) {
        return userMapper.delete(id);
    }

    public int update(User user) {
        return userMapper.update(user);
    }

    public int updateSelective(User user) {
        return userMapper.updateSelective(user);
    }

    public PageInfo<User> query(User user) {
        if(user != null && user.getPage() != null){
            PageHelper.startPage(user.getPage(),user.getLimit());
        }
        return new PageInfo<User>(userMapper.query(user));
    }

    public User detail(Integer id) {
        return userMapper.detail(id);
    }

    public int count(User user) {
        return userMapper.count(user);
    }

    public User login(String userName, String userPwd, Integer userType){
        return userMapper.login(userName, userPwd, userType);
    }

    // 注册账号：账号已存在时返回0，成功时返回1；默认注册为学生身份
    public int register(User user){
        String userName = user.getUserName().trim();
        if (userMapper.getByUserName(userName) != null){       // 账号已被占用
            return 0;
        }
        user.setUserName(userName);
        user.setUserType(0);                                  // 默认身份：学生
        return userMapper.create(user);
    }

    /**
     * 批量禁用账号：将user_type置为3
     * @param ids 逗号分隔的用户id串
     * @param operator 当前操作者（用于防止把自己禁用）
     * @return 实际禁用的账号数量
     * @throws MyException 参数不合法时抛出，由全局异常处理返回给前端
     */
    public int disable(String ids, User operator){
        String[] arr = ids.split(",");
        int row = 0;
        for (String s : arr){
            if(StringUtils.isEmpty(s)){
                continue;
            }
            User user = userMapper.detail(Integer.parseInt(s));
            if (user == null){                               // 账号不存在，跳过
                continue;
            }
            if (user.getId().equals(operator.getId())){      // 不能禁用自己
                throw new MyException("不能禁用自己的账号哦");
            }
            if (user.getUserType() != null && user.getUserType() == 2){    // 不能禁用后勤中心账号
                throw new MyException("后勤中心账号" + user.getUserName() + "不允许禁用");
            }
            if (user.getUserType() != null && user.getUserType() == 3){    // 已是禁用状态，无需重复操作
                continue;
            }
            User param = new User();
            param.setId(user.getId());
            param.setUserType(3);                            // 3表示禁用
            userMapper.updateSelective(param);
            row++;
        }
        return row;
    }

    /**
     * 启用账号：将user_type恢复为指定身份（学生=0或宿管员=1）
     */
    public int enable(Integer id, Integer userType){
        User user = userMapper.detail(id);
        if (user == null){
            throw new MyException("没有找到这个账号，可能已被删除");
        }
        if (user.getUserType() == null || user.getUserType() != 3){        // 仅禁用状态需要启用
            throw new MyException("这个账号是正常的，不需要启用哦");
        }
        if (userType == null || (userType != 0 && userType != 1)){         // 只能恢复为学生或宿管员
            throw new MyException("请选择恢复后的身份（学生或宿管员）");
        }
        User param = new User();
        param.setId(id);
        param.setUserType(userType);
        return userMapper.updateSelective(param);
    }

    // 查询某个房间住了多少人
    public int queryLiverAmount(Integer roomId){
        return userMapper.queryLiverAmount(roomId);
    }

    // 查询某栋楼住了多少人
    public int buildingActualStudentAmount(Integer buildingId){
        return userMapper.buildingActualStudentAmount(buildingId);
    }


}
