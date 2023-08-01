package com.example.service.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.ErrorCode;
import com.example.exception.BusinessException;
import com.example.model.domain.User;
import com.example.model.dto.UserDTO;
import com.example.model.request.user.UpdateRequest;
import com.example.service.UserService;
import com.example.mapper.UserMapper;
import com.example.utils.TokenUtils;
import org.springframework.stereotype.Service;

/**
* @author cheny
* @description 针对表【user】的数据库操作Service实现
* @createDate 2023-06-20 11:11:24
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    /**
     * @author chenyim
     * @date 2023/6/20 15:26
     * @Description  校验登录信息，并返回token
     * param user
     */
    @Override
    public UserDTO checkPwd(User user) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<User>();
        queryWrapper.eq("user_name",user.getUserName());
        User byId = this.getOne(queryWrapper);
        UserDTO userDTO = new UserDTO();
        //不存在该账号
        if(byId == null)
            return null;
        //密码错误
        if( !user.getPassword().equals(byId.getPassword()) ){
            byId.setId(String.valueOf(0));
            userDTO.setUser(byId);
            return userDTO;
        }
        userDTO.setUser(byId);
        userDTO.setToken(TokenUtils.getToken(byId.getId(),byId.getPassword()));
        return userDTO;
    }

    /**
     * @author chenyim
     * @date 2023/6/25 8:55
     * @Description  检查token是否过期
     */
    @Override
    public boolean checkToken(UserDTO userDTO) {
        String token = userDTO.getToken();
        //获取token的userid
        String userId;
        try {
            //解密获取
            userId = JWT.decode(token).getAudience().get(0); //得到token中的userid载荷
        } catch (JWTDecodeException j) {
            throw new BusinessException(ErrorCode.TOKEN_ILLEGAL,"token验证失败，重新登陆");
        }

        //根据userid查询数据库
        User user = this.getById(userId);
        if(user == null){
            throw new BusinessException(ErrorCode.TOKEN_ILLEGAL,"没有此用户");
        }
        // 用户密码加签验证 token
        JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(user.getPassword())).build();
        try {
            jwtVerifier.verify(token);
        } catch (JWTVerificationException e) {
            throw new BusinessException(ErrorCode.TOKEN_ILLEGAL,"token过期");
        }
        return true;
    }

    /**
     * @author chenyim
     * @date 2023/6/20 16:41
     * @Description  qq登录，刷新token
     */
    @Override
    public UserDTO checkQQLogin(User user) {
        User curUser = this.getById(user.getId());
        //该用户是新用户，则先写入数据库
        if(curUser == null){
            user.setMoney(50.0);
            this.save(user);
            curUser = user;
        }
        //设置token
        UserDTO userDTO = new UserDTO();
        userDTO.setUser(curUser);
        userDTO.setToken(TokenUtils.getToken(curUser.getId(),curUser.getPassword()));
        //用户是老用户
        return userDTO;
    }

    /**
     * @author chenyim
     * @date 2023/6/20 16:54
     * @Description  用户账号密码注册
     */
    @Override
    public User registerByUserName(User user) {
        user.setMoney(50.0);
        this.save(user);
        return user;
    }

    /**
     * @author chenyim
     * @date 2023/6/20 18:45
     * @Description  检查用户名是否重复
     * return true:重复  false：不重复
     */
    @Override
    public boolean checkUserName(String userName) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_name",userName);
        User one = this.getOne(queryWrapper);
        return one != null;
    }

    /**
     * @author chenyim
     * @date 2023/6/20 19:10
     * @Description  更改用户信息。先校验密码是否正确
     */
    @Override
    public boolean updateOneUser(UpdateRequest updateRequest) {
        User curUser = this.getById(updateRequest.getUser().getId());
        if(updateRequest.getUser().getPassword().equals(curUser.getPassword())){//密码一致
            updateRequest.getUser().setPassword( updateRequest.getPassword() );
            this.updateById(updateRequest.getUser());
            return true;
        }
        return false;
    }


}




