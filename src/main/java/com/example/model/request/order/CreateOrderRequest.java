package com.example.model.request.order;

import com.example.model.domain.User;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @Author: chenyim
 * @CreateTime: 2023-06-22  11:01
 * @Description: 用户创造一个订单
 */
@ApiModel(value = "创造订单请求")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrderRequest {
    @ApiModelProperty(value = "标题",access = "true")
    private String title;
    @ApiModelProperty(value = "内容",access = "true")
    private String content;
    @ApiModelProperty(value = "价格",access = "true")
    private double price;
    @ApiModelProperty(value = "地址",access = "true")
    private String address;
    @ApiModelProperty(value = "标签",access = "true")
    private String label;
    @ApiModelProperty(value = "期限",access = "true")
    private String deadLine;
    @ApiModelProperty(value = "用户id",access = "true")
    private String hostId;
    @ApiModelProperty(value = "标题",access = "false")
    private String pictureUrls;
    @ApiModelProperty(value = "用户信息",access = "true")
    private User user;
}
