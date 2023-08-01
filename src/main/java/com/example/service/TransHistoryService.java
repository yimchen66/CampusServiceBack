package com.example.service;

import com.example.model.domain.TransHistory;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.model.domain.TransOrder;
import com.example.model.dto.TransHistoryDTO;

import java.util.List;

/**
 * @author cheny
 * @description 针对表【trans_history】的数据库操作Service
 * @createDate 2023-06-28 15:34:52
 */
public interface TransHistoryService extends IService<TransHistory> {
    public void updateTransHistory(TransOrder transOrder);

    List<TransHistoryDTO> getTransHistory(String userId);
}
