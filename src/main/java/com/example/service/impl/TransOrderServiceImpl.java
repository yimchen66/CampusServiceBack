package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.ErrorCode;
import com.example.exception.BusinessException;
import com.example.model.domain.TransOrder;
import com.example.model.dto.UserTransOrderDTO;
import com.example.service.OrdersService;
import com.example.service.TransOrderService;
import com.example.mapper.TransOrderMapper;
import com.example.service.UserService;
import com.example.utils.TimeUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author cheny
 * @description 针对表【trans_order】的数据库操作Service实现
 * @createDate 2023-06-22 16:17:56
 */
@Service
public class TransOrderServiceImpl extends ServiceImpl<TransOrderMapper, TransOrder>
        implements TransOrderService{
    @Autowired
    private TransOrderService transOrderService;
    @Autowired
    private OrdersService ordersService;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private UserService userService;
    @Autowired
    private TransOrderMapper transOrderMapper;

    /**
     * @author chenyim
     * @date 2023/6/22 16:24
     * @Description  订单某一方确认完成订单
     */
    @Override
    public TransOrder finishOrder(String userId, String orderId) {
        TransOrder transOrder = transOrderService.getById(orderId);

        //如果请求方是发起者
        if(userId.equals(transOrder.getHostId())){
            //重复确认
            if(transOrder.getHostState() == 1)
                throw new BusinessException(ErrorCode.OPERATION_ERROR,"请勿重复确认");

            transOrder.setHostState(1);
            transOrder.setHostFinishTime(TimeUtil.getCurrentTime());
        }else{
            //重复确认
            if(transOrder.getAcceptState() == 1)
                throw new BusinessException(ErrorCode.OPERATION_ERROR,"请勿重复确认");

            transOrder.setAcceptState(1);
            transOrder.setAcceptFinishTime(TimeUtil.getCurrentTime());
        }

        //发送消息队列异步写入数据库
        rabbitTemplate.convertAndSend("trans_order_update",transOrder);
        //判断，如果双方都完成，那么将钱异步的发送到接收者
        if(transOrder.getHostState() == 1 && transOrder.getAcceptState() == 1)
            rabbitTemplate.convertAndSend("send_money",transOrder);

        return transOrder;
    }

    /**
     * @author chenyim
     * @date 2023/6/22 18:38
     * @Description  获取用户交易中的订单
     */
    @Override
    public List<UserTransOrderDTO> getUserTransOrder(String requestUserId, String requestType,
                                                     int pageNo, int pageNumber) {
        QueryWrapper<TransOrder> q = new QueryWrapper<>();
        boolean isHost;
        if(requestType.equals("发起")){
            isHost = true;
            q.lambda().eq(TransOrder::getHostId,requestUserId);
        }
        else{
            isHost = false;
            q.lambda().eq(TransOrder::getAcceptId,requestUserId);
        }
        //分页查询
        Page<TransOrder> page = new Page<>(pageNo,pageNumber);
        Page<TransOrder> page1 = transOrderMapper.selectPage(page, q);
        List<TransOrder> list = page1.getRecords();

        return list.stream()
                .map(transOrder -> new UserTransOrderDTO(
                        ordersService.getById(transOrder.getId()),
                        transOrder,
                        userService.getById(isHost ? transOrder.getAcceptId() : transOrder.getHostId())
                ))
                .collect(Collectors.toList());
    }
}
