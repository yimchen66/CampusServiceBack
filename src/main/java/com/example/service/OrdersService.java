package com.example.service;

import com.example.model.domain.Orders;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.model.dto.DetailedOrderDTO;
import com.example.model.request.order.CreateOrderRequest;
import com.example.model.request.order.UpdateOrderRequest;

import java.util.List;

/**
 * @author cheny
 * @description 针对表【orders】的数据库操作Service
 * @createDate 2023-06-21 12:00:51
 */
public interface OrdersService extends IService<Orders> {
    List<Orders> getPagedOrderList(int pages, int pageNumber, String msg, String column, String sortedType);
    DetailedOrderDTO getDetailedOrderByAdmin(String id);
    DetailedOrderDTO getDetailedOrderByUser(String id);
    boolean acceptOrder(DetailedOrderDTO detailedOrderDTO);
    Orders createOrderByUser(CreateOrderRequest createOrderRequest);
    Orders updateOrderByUser(UpdateOrderRequest updateOrderRequest);

    List<DetailedOrderDTO> getUserTrans(String userId);
    List<DetailedOrderDTO> getUserFinish(String userId);
}
