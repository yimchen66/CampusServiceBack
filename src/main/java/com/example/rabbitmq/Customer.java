package com.example.rabbitmq;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.model.domain.*;
import com.example.model.dto.UpdateOrderDTO;
import com.example.service.*;
import com.example.utils.RedisUtil;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author: chenyim
 * @CreateTime: 2023-06-21  20:54
 * @Description: 消息队列，消费者
 */
@Component
public class Customer {
    @Autowired
    private TransOrderService transOrderService;
    @Autowired
    private RecommandOrderService recommandOrderService;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private OrdersService ordersService;
    @Autowired
    private UserService userService;
    @Autowired
    private TransHistoryService transHistoryService;
    /**
     * @author chenyim
     * @date 2023/6/21 20:55
     * @Description  增加trans_order的消费者
     */
    @RabbitListener(queuesToDeclare = @Queue(value = "trans_order"))
    public void transOrderListener(TransOrder transOrder){
        if(transOrderService.getById(transOrder.getId()) == null)
            transOrderService.save(transOrder);
    }

    /**
     * @author chenyim
     * @date 2023/6/22 10:19
     * @Description  删除推荐订单的消费者
     */
    @RabbitListener(queuesToDeclare = @Queue(value = "del_recommend_order"))
    public void delRecommendOrder(String id){
        //判断有没有
        RecommandOrder order = recommandOrderService.getById(id);
        if(order != null){
            boolean b = recommandOrderService.removeById(id);
            //更新缓存
            if(b)
                redisUtil.lRemove("recommandorder",1,order);
        }
    }

    /**
     * @author chenyim
     * @date 2023/6/22 11:28
     * @Description  创建订单
     */
    @RabbitListener(queuesToDeclare = @Queue(value = "create_order"))
    public void createOrder(Orders orders){
        ordersService.save(orders);
    }

    /**
     * @author chenyim
     * @date 2023/6/22 11:28
     * @Description  扣钱
     */
    @RabbitListener(queuesToDeclare = @Queue(value = "create_order_del_money"))
    public void delMoney(User user){
        userService.updateById(user);
    }

    /**
     * @author chenyim
     * @date 2023/6/22 16:32
     * @Description  修改trans_order的消费者
     */
    @RabbitListener(queuesToDeclare = @Queue(value = "trans_order_update"))
    public void updateTransOrder(TransOrder transOrder){
        transOrderService.updateById(transOrder);
    }

    /**
     * @author chenyim
     * @date 2023/6/22 16:45
     * @Description  交易成功后更新钱
     */
    @RabbitListener(queuesToDeclare = @Queue(value = "send_money"))
    public void upUserMoney(TransOrder transOrder){
        UpdateWrapper<User> wrapper = new UpdateWrapper<>();
        wrapper.lambda().eq(User::getId,transOrder.getAcceptId())
                .setSql("money = money + "+transOrder.getOrderMoney()+" ");
        userService.update(wrapper);
        //更新交易记录

        transHistoryService.updateTransHistory(transOrder);
    }

    /**
     * @author chenyim
     * @date 2023/6/22 22:25
     * @Description  接单后，删除两个排行榜的内容
     */
    @RabbitListener(queuesToDeclare = @Queue(value = "redis_del"))
    public void delRedisRank(Orders orders){
         orders.setVersion(null);
         String rankId = JSON.toJSONString(orders);
         String id = orders.getId();
         redisUtil.zRemove("order_hot",id);
         redisUtil.zRemove("order_ranking",rankId);
    }

    /**
     * @author chenyim
     * @date 2023/6/22 22:25
     * @Description  修改订单后，将热度榜原来的订单信息改掉，并重新入榜
     */
    @RabbitListener(queuesToDeclare = @Queue(value = "redis_update"))
    public void updateRedisRank(UpdateOrderDTO orderDTO){
        Orders orders_ori = new Orders();
        Orders orders_update = new Orders();
        if(orderDTO.getOrderId() == null){
            orders_ori = orderDTO.getOrders_ori();
            orders_update = orderDTO.getOrders_update();
        }else{
            orders_ori = ordersService.getById(orderDTO.getOrderId());
            BeanUtils.copyProperties(orders_ori,orders_update);
            orders_ori.setOrderIsRecommend(1);
            orders_update.setOrderIsRecommend(0);
        }
        orders_ori.setVersion(null);
        String rankId = JSON.toJSONString(orders_ori);
        Object obj = redisUtil.zGetScore("order_ranking", rankId);
        //原来在热度榜内,则需要先删除，再重新入榜
        if( obj != null){
            double score = (double) obj;
            redisUtil.zRemove("order_ranking",rankId);
            orders_update.setVersion(null);
            String updateRankId = JSON.toJSONString(orders_update);
            redisUtil.zIncrement("order_ranking",updateRankId,score);
        }
    }
}
