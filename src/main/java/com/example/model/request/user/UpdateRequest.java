package com.example.model.request.user;

import com.example.model.domain.User;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: chenyim
 * @CreateTime: 2023-06-20  19:24
 * @Description: TODO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "更改信息请求")
public class UpdateRequest {
    @ApiModelProperty(value = "用户信息,密码是原来的",access = "true")
    private User user;

    @ApiModelProperty(value = "新密码",access = "true")
    private String password;
}
