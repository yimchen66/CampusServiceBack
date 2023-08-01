package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.model.domain.TransHistory;
import com.example.model.domain.TransOrder;
import com.example.model.domain.User;
import com.example.model.dto.TransHistoryDTO;
import com.example.service.TransHistoryService;
import com.example.mapper.TransHistoryMapper;
import com.example.service.UserService;
import com.example.utils.TimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author cheny
 * @description 针对表【trans_history】的数据库操作Service实现
 * @createDate 2023-06-28 15:34:52
 */
@Service
public class TransHistoryServiceImpl extends ServiceImpl<TransHistoryMapper, TransHistory>
        implements TransHistoryService{
    @Autowired
    private UserService userService;

    /**
     * @author chenyim
     * @date 2023/6/28 15:37
     * @Description  更新交易记录
     */
    @Override
    public void updateTransHistory(TransOrder transOrder) {
        String hostId = transOrder.getHostId();
        String acceptId = transOrder.getAcceptId();
        Double orderMoney = transOrder.getOrderMoney();
        TransHistory transHistory = new TransHistory();

        transHistory.setAcceptId(acceptId);
        transHistory.setHostId(hostId);
        transHistory.setOrderId(transOrder.getId());
        transHistory.setFinishTime(TimeUtil.getCurrentTime());
        transHistory.setOrderMoney(orderMoney);
        this.save(transHistory);
    }

    /**
     * @author chenyim
     * @date 2023/6/28 15:52
     * @Description  获取记录
     */
    @Override
    public List<TransHistoryDTO> getTransHistory(String userId) {
        List<TransHistoryDTO> listResult = new ArrayList<>();
        QueryWrapper<TransHistory> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(TransHistory::getAcceptId,userId)
                .or()
                .eq(TransHistory::getHostId,userId);
        List<TransHistory> list = this.list(queryWrapper);
        for (TransHistory transHistory : list) {
            TransHistoryDTO historyDTO = new TransHistoryDTO();

            String hostId = transHistory.getHostId();
            //如果请求者是发起者,则显示对方头像，扣钱
            if(hostId.equals(userId)){
                User targetUser = userService.getById(transHistory.getAcceptId());
                historyDTO.setNickName(targetUser.getNickName());
                historyDTO.setPicUrl(targetUser.getFigureUrl());
                historyDTO.setOrder_money(0- transHistory.getOrderMoney());
            }else{//如果请求者是接受者,则显示对方头像，加钱
                User targetUser = userService.getById(transHistory.getHostId());
                historyDTO.setNickName(targetUser.getNickName());
                historyDTO.setPicUrl(targetUser.getFigureUrl());
                historyDTO.setOrder_money(transHistory.getOrderMoney());
            }
            historyDTO.setTime(transHistory.getFinishTime());

            listResult.add(historyDTO);
        }
        return listResult;
    }
}






