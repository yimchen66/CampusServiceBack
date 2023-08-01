package com.example.service;

import com.example.model.domain.TransOrder;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.model.dto.UserTransOrderDTO;

import java.util.List;

/**
 * @author cheny
 * @description 针对表【trans_order】的数据库操作Service
 * @createDate 2023-06-22 16:17:56
 */
public interface TransOrderService extends IService<TransOrder> {
    TransOrder finishOrder(String userId, String orderId);
    List<UserTransOrderDTO> getUserTransOrder(String requestUserId, String requestType,
                                              int pageNo, int pageNumber);
}