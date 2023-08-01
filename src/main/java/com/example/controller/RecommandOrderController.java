package com.example.controller;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.common.BaseResponse;
import com.example.common.ResultUtils;
import com.example.model.domain.Orders;
import com.example.model.domain.RecommandOrder;
import com.example.model.dto.UpdateOrderDTO;
import com.example.service.OrdersService;
import com.example.service.RecommandOrderService;
import com.example.utils.RedisUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: chenyim
 * @CreateTime: 2023-06-20  20:42
 * @Description: TODO
 */

@RestController
@RequestMapping("/main/recommand")
@Api(tags = "首页推荐订单")
public class RecommandOrderController {

    @Autowired
    private RecommandOrderService recommandOrderService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private OrdersService ordersService;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    /**
     * @author chenyim
     * @date 2023/6/20 20:43
     * @Description  获取首页的订单
     */
    @ApiOperation("获取首页的订单")
    @GetMapping()
    public BaseResponse<List<RecommandOrder>> getRecommendOrder(){
        //先检查缓存
        List<Object> recommandOrderList = redisUtil.lGet("recommandorder", 0, -1);

        //命中缓存
        if(recommandOrderList != null && recommandOrderList.size() != 0){
            System.out.println("命中缓存");
            List<RecommandOrder> list = recommandOrderList.stream()
                    .map(o -> (RecommandOrder) o)
                    .collect(Collectors.toList());
            return ResultUtils.success(list);
        }
        //查询数据库
        List<RecommandOrder> recommandOrderLists = recommandOrderService.list();
        //存入缓存
        recommandOrderLists.forEach(recommandOrder -> redisUtil.lSet("recommandorder", recommandOrder));
        System.out.println("查数据库");
        return ResultUtils.success(recommandOrderLists);
    }

    /**
     * @author chenyim
     * @date 2023/6/20 22:03
     * @Description  通过id删除某个推荐订单
     */
    @ApiOperation("通过id删除某个推荐订单")
    @DeleteMapping("/delete/one")
    public BaseResponse<String> deleteRecommendOrderById(@RequestBody RecommandOrder recommandOrder){
        //更新缓存
        redisUtil.lRemove("recommandorder",1,recommandOrder);
        //删除数据库
        recommandOrderService.removeById(recommandOrder.getId());
        //更改订单状态
        UpdateWrapper<Orders> q = new UpdateWrapper<>();
        q.eq("id",recommandOrder.getId())
         .set("order_is_recommend",0);
        ordersService.update(q);

        //更改热度榜
        UpdateOrderDTO orderDTO = new UpdateOrderDTO(null,null,recommandOrder.getId());
        rabbitTemplate.convertAndSend("redis_update", orderDTO);

        return ResultUtils.success("删除成功");
    }

    /**
     * @author chenyim
     * @date 2023/6/20 22:03
     * @Description  增加一个推荐订单
     */
    @ApiOperation("增加一个推荐订单")
    @PostMapping("/add/one/{id}")
    public BaseResponse<String> addCarouselOne(@PathVariable String id){
        //先加入数据库
        RecommandOrder recommandOrder = recommandOrderService.saveByOrderId(id);
        //更新缓存
        redisUtil.lSet("recommandorder",recommandOrder);
        return ResultUtils.success("添加成功！");
    }
}
