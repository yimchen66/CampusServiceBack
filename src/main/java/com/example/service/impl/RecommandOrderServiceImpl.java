package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.ErrorCode;
import com.example.exception.BusinessException;
import com.example.model.domain.Orders;
import com.example.model.domain.RecommandOrder;
import com.example.model.domain.User;
import com.example.model.dto.UpdateOrderDTO;
import com.example.service.OrdersService;
import com.example.service.RecommandOrderService;
import com.example.mapper.RecommandOrderMapper;
import com.example.service.UserService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author cheny
 * @description 针对表【recommand_order】的数据库操作Service实现
 * @createDate 2023-06-20 11:11:19
 */
@Service
public class RecommandOrderServiceImpl extends ServiceImpl<RecommandOrderMapper, RecommandOrder>
        implements RecommandOrderService{

    @Autowired
    private OrdersService ordersService;
    @Autowired
    private UserService userService;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * @author chenyim
     * @date 2023/6/21 10:49
     * @Description  通过选择id来增加一个推荐订单
     */
    @Override
    public RecommandOrder saveByOrderId(String id) {
        Orders order = ordersService.getById(id);
        //订单不存在
        if(order == null)
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"订单不存在");
        //订单已经是推荐状态
        if(order.getOrderIsRecommend() == 1)
            throw new BusinessException(ErrorCode.SAME_USERNAME_ERROR,"订单已经是推荐状态");

        //保存原始order
        Orders orders_ori = new Orders();
        BeanUtils.copyProperties(order,orders_ori);

        User user = userService.getById(order.getHostId());
        RecommandOrder recommandOrder = new RecommandOrder(
                order.getId(),
                order.getOrderTitle(),
                order.getOrderPictures(),
                Integer.parseInt(order.getOrderHot().toString()),
                order.getOrderLabel(),
                user.getNickName(),
                user.getFigureUrl(),
                order.getOrderMoney()
        );
        this.save(recommandOrder);
        //将order状态设为已推荐状态
        order.setOrderIsRecommend(1);
        boolean b = ordersService.updateById(order);
        if(!b)
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"用户已经接单或者更改，无法继续推荐");

        //推荐成功，更改缓存的热度榜
        UpdateOrderDTO orderDTO = new UpdateOrderDTO(orders_ori,order,null);
        rabbitTemplate.convertAndSend("redis_update",orderDTO);


        return recommandOrder;
    }
}
