package com.example.mapper;

import com.example.model.domain.Orders;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author cheny
* @description 针对表【orders】的数据库操作Mapper
* @createDate 2023-06-21 19:53:16
* @Entity com.example.model.domain.Orders
*/
@Mapper
public interface OrdersMapper extends BaseMapper<Orders> {

}




