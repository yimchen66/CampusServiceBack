package com.example.controller;

import com.example.common.BaseResponse;
import com.example.common.ResultUtils;
import com.example.model.dto.TransHistoryDTO;
import com.example.service.TransHistoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author: chenyim
 * @CreateTime: 2023-06-28  15:45
 * @Description: TODO
 */
@RestController
@RequestMapping("/trans/history")
@Api(tags = "交易记录")
public class TransHistoryController {
    @Autowired
    private TransHistoryService transHistoryService;

    /**
     * @author chenyim
     * @date 2023/6/28 15:46
     * @Description  获取某个用户交易记录
     */
    @ApiOperation("获取某个用户交易记录")
    @GetMapping("/{userId}")
    public BaseResponse<List<TransHistoryDTO>> getTransHistory(@PathVariable String userId){
        List<TransHistoryDTO> transHistory = transHistoryService.getTransHistory(userId);
        return ResultUtils.success(transHistory);
    }
}
