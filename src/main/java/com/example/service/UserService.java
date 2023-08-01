package com.example.service;

import com.example.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.model.dto.UserDTO;
import com.example.model.request.user.UpdateRequest;

/**
* @author cheny
* @description 针对表【user】的数据库操作Service
* @createDate 2023-06-20 11:11:24
*/
public interface UserService extends IService<User> {
    UserDTO checkPwd(User user);
    boolean checkToken(UserDTO userDTO);
    UserDTO checkQQLogin(User user);
    User registerByUserName(User user);
    boolean checkUserName(String userName);
    boolean updateOneUser(UpdateRequest updateRequest);
}
