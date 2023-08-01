package com.example.model.request.order;

import com.example.model.domain.Orders;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: chenyim
 * @CreateTime: 2023-06-22  11:01
 * @Description: 用户更新订单
 */
@ApiModel(value = "更新订单请求")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateOrderRequest {
    @ApiModelProperty(value = "订单id",access = "true")
    private String id;
    @ApiModelProperty(value = "标题",access = "true")
    private String orderTitle;
    @ApiModelProperty(value = "内容",access = "true")
    private String orderContent;
    @ApiModelProperty(value = "地址",access = "true")
    private String orderAddress;
    @ApiModelProperty(value = "标签",access = "true")
    private String orderLabel;
    @ApiModelProperty(value = "期限",access = "true")
    private String orderDeadline;
    @ApiModelProperty(value = "图片",access = "false")
    private String orderPictures;
    @ApiModelProperty(value = "原来的订单",access = "true")
    private Orders orders;
}
