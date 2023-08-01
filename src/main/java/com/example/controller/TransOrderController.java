package com.example.controller;

import cn.hutool.core.util.StrUtil;
import com.example.common.BaseResponse;
import com.example.common.ErrorCode;
import com.example.common.ResultUtils;
import com.example.exception.BusinessException;
import com.example.model.domain.TransOrder;
import com.example.model.dto.UserTransOrderDTO;
import com.example.model.request.trans.TransOrderFinishRequest;
import com.example.model.request.trans.UserTransOrderRequest;
import com.example.service.TransOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author: chenyim
 * @CreateTime: 2023-06-22  15:43
 * @Description: 交易订单
 */
@RestController
@RequestMapping("/trans")
@Api(tags = "订单交易")
public class TransOrderController {

    @Autowired
    private TransOrderService transOrderService;

    /**
     * @author chenyim
     * @date 2023/6/22 15:44
     * @Description  某方确认完成订单
     */
    @ApiOperation("某方确认完成订单")
    @PostMapping("/finish")
    public BaseResponse<TransOrder> finishOrder(@RequestBody TransOrderFinishRequest transOrderFinishRequest){
        String orderId = transOrderFinishRequest.getOrderID();
        String hostId = transOrderFinishRequest.getHostId();
        if(StrUtil.isBlank(orderId) || StrUtil.isBlank(hostId))
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数不能为空");
        TransOrder transOrder = transOrderService.finishOrder(hostId, orderId);
        return ResultUtils.success(transOrder);
    }

    /**
     * @author chenyim
     * @date 2023/6/22 16:38
     * @Description  用户获取自己相关的订单
     */
    @ApiOperation("用户获取自己相关的订单")
    @PostMapping("/getall")
    public BaseResponse<List<UserTransOrderDTO>> getUserTransOrders(@RequestBody
                                                                    UserTransOrderRequest userTransOrderRequest){
        String requestType = userTransOrderRequest.getRequestType();
        String requestUserId = userTransOrderRequest.getRequestUserId();
        int pageNo = userTransOrderRequest.getPageNo();
        int pageNumber = userTransOrderRequest.getPageNumber();

        //检测参数
        if(StrUtil.isBlank(requestUserId) || StrUtil.isBlank(requestType)
                            || StrUtil.isBlank(String.valueOf(pageNo)) || StrUtil.isBlank(String.valueOf(pageNumber)))
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数请求错误");

        List<UserTransOrderDTO> userTransOrder = transOrderService.getUserTransOrder(requestUserId, requestType,
                pageNo,pageNumber);
        return ResultUtils.success(userTransOrder);
    }


}
