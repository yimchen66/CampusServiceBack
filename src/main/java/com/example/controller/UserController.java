package com.example.controller;


import com.example.common.BaseResponse;
import com.example.common.ErrorCode;
import com.example.common.ResultUtils;
import com.example.model.domain.User;
import com.example.model.dto.UserDTO;
import com.example.model.request.user.UpdateRequest;
import com.example.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@Api(tags = "user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * @author chenyim
     * @date 2023/6/21 9:13
     * @Description  账号密码登录后，刷新token
     */
    @ApiOperation("账号密码登录,刷新token过期时间")
    @PostMapping("/login")
    public BaseResponse<UserDTO> userLogin(@RequestBody User user){
        UserDTO userDTO = userService.checkPwd(user);
        if(userDTO != null){
            System.out.println(userDTO);
            if(userDTO.getUser().getId().equals("0"))//密码错误
                return ResultUtils.error(ErrorCode.PASSWORD_ERROR);
            //登录成功
            return ResultUtils.success(userDTO,"登录成功");
        }else{
            return new BaseResponse<>(ErrorCode.NO_USERNAME_ERROR);
        }
    }

    @ApiOperation("qq登录，返回用户信息")
    @PostMapping("/qqlogin")
    public BaseResponse<UserDTO> userQQLogin(@RequestBody User user){
        UserDTO userDTO = userService.checkQQLogin(user);
        return ResultUtils.success(userDTO);
    }


    /**
     * @author chenyim
     * @date 2023/6/25 8:49
     * @Description  检查token是否过期
     */
    @ApiOperation("检查token是否过期")
    @PostMapping("/checktoken")
    public BaseResponse<Boolean> checkToken(@RequestBody UserDTO userDTO){
        boolean b = userService.checkToken(userDTO);
        return ResultUtils.success(b);
    }


    @ApiOperation("用户账号密码注册")
    @PostMapping("/register")
    public BaseResponse<User> userRegisterByUserName(@RequestBody User user){
        User curUser = userService.registerByUserName(user);
        return ResultUtils.success(curUser);
    }

    @ApiOperation("检查用户名是否重复")
    @GetMapping("/check/{userName}")
    public BaseResponse<String> checkUserName(@PathVariable String userName){
        if(userService.checkUserName(userName))
            return ResultUtils.error(ErrorCode.SAME_USERNAME_ERROR);
        else
            return ResultUtils.success("可用");
    }

    @ApiOperation("获取某个人的基本信息")
    @PostMapping("/get/one")
    public BaseResponse<User> getOneUser(@RequestBody User user){
        User user1 = userService.getById(user.getId());
        return ResultUtils.success(user1);
    }

    @ApiOperation("修改某个人的基本信息")
    @PostMapping("/update")
    public BaseResponse<User> updateOneUser(@RequestBody UpdateRequest updateRequest){
        if(userService.updateOneUser(updateRequest)){
            updateRequest.getUser().setPassword(updateRequest.getPassword());
            return ResultUtils.success(updateRequest.getUser(),"修改成功");
        }
        else
            return ResultUtils.error(ErrorCode.PASSWORD_ERROR);
    }

}



