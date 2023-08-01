package com.example.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.example.model.domain.TransOrder;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.ErrorCode;
import com.example.exception.BusinessException;
import com.example.model.domain.Orders;
import com.example.model.domain.User;
import com.example.model.dto.DetailedOrderDTO;
import com.example.model.dto.UpdateOrderDTO;
import com.example.model.request.order.CreateOrderRequest;
import com.example.model.request.order.UpdateOrderRequest;
import com.example.service.OrdersService;
import com.example.mapper.OrdersMapper;
import com.example.service.TransOrderService;
import com.example.service.UserService;
import com.example.utils.TimeUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author cheny
 * @description 针对表【orders】的数据库操作Service实现
 * @createDate 2023-06-21 12:00:51
 */
@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders>
        implements OrdersService{

    @Autowired
    private OrdersMapper ordersMapper;
    @Autowired
    private UserService userService;
    @Autowired
    private TransOrderService transOrderService;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private OrdersService ordersService;


    /**
     * @author chenyim
     * @date 2023/6/21 16:34
     * @Description  获取全部订单
     */
    @Override
    public List<Orders> getPagedOrderList(int pages, int pageNumber, String msg,
                                          String column, String sortedType) {
        Page<Orders> page = new Page<>(pages, pageNumber);
        if(!(StrUtil.isBlank(column) || StrUtil.isBlank(sortedType)))
            if(sortedType.equals("desc"))
                page.addOrder(OrderItem.desc(column));
            else
                page.addOrder(OrderItem.asc(column));

        if(msg.equals("包括"))
            page = ordersMapper.selectPage(page, null);
        else{   //不包括已经被接单的
            QueryWrapper<Orders> q = new QueryWrapper<>();
            q.lambda().eq(Orders::getOrderState,0);
            page  = ordersMapper.selectPage(page, q);
        }

            return page.getRecords();

    }

    /**
     * @author chenyim
     * @date 2023/6/21 16:49
     * @Description  获取某个订单的详细情况，包括用户信息
     */
    @Override
    public DetailedOrderDTO getDetailedOrderByAdmin(String id) {
        DetailedOrderDTO detailedOrderDTO = new DetailedOrderDTO();
        Orders order = this.getById(id);
        if(order == null)
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"订单不存在");
        User hostUser = userService.getById(order.getHostId());
        detailedOrderDTO.setOrders(order);
        detailedOrderDTO.setHostUser(hostUser);
        if(order.getOrderState() == 1){
            detailedOrderDTO.setOrderState(1);
            detailedOrderDTO.setAcceptedUser(userService.getById(order.getAcceptId()));
            detailedOrderDTO.setTransOrder(transOrderService.getById(id));
        }
        return detailedOrderDTO;
    }
    /**
     * @author chenyim
     * @date 2023/6/21 16:49
     * @Description  用户获取某个订单的详细情况，一定是未结单状态
     */
    @Override
    public DetailedOrderDTO getDetailedOrderByUser(String id) {
        DetailedOrderDTO detailedOrderDTO = new DetailedOrderDTO();
        Orders order = this.getById(id);
        if(order == null)
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"订单不存在");
        if(order.getOrderState() == 1)
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "订单已被接受");

        User hostUser = userService.getById(order.getHostId());
        detailedOrderDTO.setOrders(order);
        detailedOrderDTO.setHostUser(hostUser);
        return detailedOrderDTO;
    }

    /**
     * @author chenyim
     * @date 2023/6/21 20:02
     * @Description  用户接受某个订单,更新orders表，新增trans_order表
     * 判断是不是首页推荐的订单，如果是，则将其删除
     */
    @Override
    public boolean acceptOrder(DetailedOrderDTO detailedOrderDTO) {
        Orders orders = detailedOrderDTO.getOrders();
        Orders orders_ori = new Orders();
        BeanUtils.copyProperties(orders,orders_ori);
        User hostUser = detailedOrderDTO.getHostUser();
        User acceptedUser = detailedOrderDTO.getAcceptedUser();

        //判断是不是自己接在自己的单
        if(hostUser.getId().equals(acceptedUser.getId()))
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"这是你自己的订单");

        int isRecommend = orders.getOrderIsRecommend();

        orders.setOrderState(1);
        orders.setOrderIsRecommend(0);
        orders.setAcceptId(acceptedUser.getId());

        //更新orders表，乐观锁机制
        boolean b = this.updateById(orders);
        if(!b)
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"已被被人抢单");

        //新添数据到trans_order表
        TransOrder transOrder = new TransOrder(
                orders.getId(),
                hostUser.getId(),
                acceptedUser.getId(),
                TimeUtil.getCurrentTime(),
                0,0,
                null,null,
                orders.getOrderMoney()
        );
        //发送到消息队列
        rabbitTemplate.convertAndSend("trans_order",transOrder);

        //如果是推荐的被接单，则去删除
        if(isRecommend == 1)
            rabbitTemplate.convertAndSend("del_recommend_order",orders.getId());

        //将缓存中排行榜的相关内容删除
        rabbitTemplate.convertAndSend("redis_del",orders_ori);
        return true;
    }

    /**
     * @author chenyim
     * @date 2023/6/22 14:33
     * @Description  用户新建订单
     */
    @Override
    public Orders createOrderByUser(CreateOrderRequest createOrderRequest) {
        String title = createOrderRequest.getTitle();
        String content = createOrderRequest.getContent();
        double price = createOrderRequest.getPrice();
        String label = createOrderRequest.getLabel();
        String address = createOrderRequest.getAddress();
        String deadLine = createOrderRequest.getDeadLine();
        String hostId = createOrderRequest.getHostId();
        String pictureUrls = createOrderRequest.getPictureUrls();
        User user = createOrderRequest.getUser();
        //判空
        if(StrUtil.isBlank(title) || StrUtil.isBlank(content)
                || StrUtil.isBlank(String.valueOf(price)) || StrUtil.isBlank(hostId)
                || StrUtil.isBlank(label) || StrUtil.isBlank(String.valueOf(deadLine))
                || StrUtil.isBlank(address))
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数不完整");

        //判断钱够不够
        if(price > user.getMoney())
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"钱不够");
        user.setMoney(user.getMoney() - price);


        Orders orders = new Orders();
        orders.setOrderAddress(address);
        orders.setOrderContent(content);
        orders.setOrderCreateTime(TimeUtil.getCurrentTime());
        orders.setHostId(hostId);
        orders.setOrderDeadline(deadLine);
        orders.setOrderLabel(label);
        orders.setOrderTitle(title);
        orders.setOrderMoney(price);
        orders.setOrderPictures(pictureUrls);

        this.save(orders);
        QueryWrapper<Orders> q = new QueryWrapper<>();
        q.eq("host_id",orders.getHostId())
                .eq("order_create_time",orders.getOrderCreateTime());

        //扣钱操作发送到消息队列
        rabbitTemplate.convertAndSend("create_order_del_money",user);
        return this.getOne(q);
    }

    /**
     * @author chenyim
     * @date 2023/6/22 14:33
     * @Description  用户修改订单
     */
    @Override
    public Orders updateOrderByUser(UpdateOrderRequest updateOrderRequest) {
        String title = updateOrderRequest.getOrderTitle();
        String content = updateOrderRequest.getOrderContent();
        String label = updateOrderRequest.getOrderLabel();
        String address = updateOrderRequest.getOrderAddress();
        String deadLine = updateOrderRequest.getOrderDeadline();
        String pictureUrls = updateOrderRequest.getOrderPictures();
        String id = updateOrderRequest.getId();
        //判空
        if(StrUtil.isBlank(title) || StrUtil.isBlank(content)
                || StrUtil.isBlank(id)
                || StrUtil.isBlank(label) || StrUtil.isBlank(String.valueOf(deadLine))
                || StrUtil.isBlank(address))
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数不完整");

        //利用乐观锁机制
        Orders orders = updateOrderRequest.getOrders();
        if(orders.getOrderIsRecommend() == 1)
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"已推荐状态下不可修改");

        //原来的订单信息orders_ori
        Orders orders_ori = new Orders();
        BeanUtils.copyProperties(orders, orders_ori);

        //更新后的订单信息orders
        BeanUtils.copyProperties(updateOrderRequest,orders);
        boolean b = this.updateById(orders);
        //可能有用户在此期间接单，则修改失败
        if(!b)
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"操作失败，有人已经接单");

        //修改成功，将热度榜上的订单修改
        UpdateOrderDTO orderDTO = new UpdateOrderDTO(orders_ori,orders,null);
        rabbitTemplate.convertAndSend("redis_update", orderDTO);
        return orders;
    }


    /**
     * @author chenyim
     * @date 2023/6/28 10:42
     * @Description  获取某人正在交易的订单
     */
    @Override
    public List<DetailedOrderDTO> getUserTrans(String userId) {
        List<DetailedOrderDTO> listResult = new ArrayList<>();
        QueryWrapper<TransOrder> q = new QueryWrapper<>();
        q.lambda()
                .eq(TransOrder::getAcceptId,userId)
                .or()
                .eq(TransOrder::getHostId,userId);
        q.and(q2 -> q2.lambda()
                .eq(TransOrder::getAcceptState,0)
                .or()
                .eq(TransOrder::getHostState,0));

        List<TransOrder> list = transOrderService.list(q);
        for (TransOrder transOrder : list) {
            String hostId = transOrder.getHostId();
            String acceptId = transOrder.getAcceptId();
            String orderId = transOrder.getId();

            Orders orders = ordersService.getById(orderId);
            User hostUser = userService.getById(hostId);
            User acceptUser = userService.getById(acceptId);
            DetailedOrderDTO detailedOrderDTO = new DetailedOrderDTO();
            detailedOrderDTO.setTransOrder(transOrder);
            detailedOrderDTO.setOrderState(1);
            detailedOrderDTO.setAcceptedUser(acceptUser);
            detailedOrderDTO.setOrders(orders);
            detailedOrderDTO.setHostUser(hostUser);
            listResult.add(detailedOrderDTO);
        }
        return listResult;
    }

    /**
     * @author chenyim
     * @date 2023/6/28 10:54
     * @Description  获取某人已经完成的订房
     */
    @Override
    public List<DetailedOrderDTO> getUserFinish(String userId) {
        List<DetailedOrderDTO> listResult = new ArrayList<>();
        QueryWrapper<TransOrder> q = new QueryWrapper<>();
        q.lambda().eq(TransOrder::getAcceptState,1)
                        .eq(TransOrder::getHostState,1);

        q.lambda()
                .eq(TransOrder::getAcceptId,userId)
                .or()
                .eq(TransOrder::getHostId,userId);


        List<TransOrder> list = transOrderService.list(q);
        for (TransOrder transOrder : list) {
            String hostId = transOrder.getHostId();
            String acceptId = transOrder.getAcceptId();
            String orderId = transOrder.getId();
            Orders orders = ordersService.getById(orderId);
            User hostUser = userService.getById(hostId);
            User acceptUser = userService.getById(acceptId);
            DetailedOrderDTO detailedOrderDTO = new DetailedOrderDTO();
            detailedOrderDTO.setTransOrder(transOrder);
            detailedOrderDTO.setOrderState(1);
            detailedOrderDTO.setAcceptedUser(acceptUser);
            detailedOrderDTO.setOrders(orders);
            detailedOrderDTO.setHostUser(hostUser);
            listResult.add(detailedOrderDTO);
            System.out.println(transOrder);
        }
        return listResult;
    }

}
