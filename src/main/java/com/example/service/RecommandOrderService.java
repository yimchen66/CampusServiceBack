package com.example.service;

import com.example.model.domain.RecommandOrder;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author cheny
 * @description 针对表【recommand_order】的数据库操作Service
 * @createDate 2023-06-20 11:11:19
 */
public interface RecommandOrderService extends IService<RecommandOrder> {
    RecommandOrder saveByOrderId(String id);
}
