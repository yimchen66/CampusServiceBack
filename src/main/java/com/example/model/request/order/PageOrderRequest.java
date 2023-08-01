package com.example.model.request.order;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: chenyim
 * @CreateTime: 2023-06-21  15:49
 * @Description: 分页订单请求
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "分页订单请求")
public class PageOrderRequest {
    @ApiModelProperty(value = "第几页",access = "true")
    private int pages;

    @ApiModelProperty(value = "每页数量",access = "true")
    private int pageNumber;

    @ApiModelProperty(value = "排序的列名",access = "false")
    private String column;

    @ApiModelProperty(value = "序列",access = "false")
    private String sortedType;

    public PageOrderRequest(int pages, int pageNumber) {
        this.pages = pages;
        this.pageNumber = pageNumber;
    }
}
