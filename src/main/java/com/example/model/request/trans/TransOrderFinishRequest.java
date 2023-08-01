package com.example.model.request.trans;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: chenyim
 * @CreateTime: 2023-06-22  15:52
 * @Description: 完成订单请求
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("完成订单请求")
public class TransOrderFinishRequest {
    @ApiModelProperty(value = "请求用户id",access = "true")
    private String hostId;
    @ApiModelProperty(value = "订单id",access = "true")
    private String orderID;
}
