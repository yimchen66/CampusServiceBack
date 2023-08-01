package com.example.model.request.trans;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: chenyim
 * @CreateTime: 2023-06-22  18:23
 * @Description: 用户查询请求体
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("用户查询请求体")
public class UserTransOrderRequest {
    @ApiModelProperty(value = "请求类型 发起/接受",access = "true")
    private String requestType;
    @ApiModelProperty(value = "用户的id",access = "true")
    private String requestUserId;
    @ApiModelProperty(value = "页码",access = "true")
    private int pageNo;
    @ApiModelProperty(value = "一页个数",access = "true")
    private int pageNumber;

}
