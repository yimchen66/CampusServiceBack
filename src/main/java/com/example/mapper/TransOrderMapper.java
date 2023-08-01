package com.example.mapper;

import com.example.model.domain.TransOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author cheny
* @description 针对表【trans_order】的数据库操作Mapper
* @createDate 2023-06-22 16:55:44
* @Entity com.example.model.domain.TransOrder
*/
@Mapper
public interface TransOrderMapper extends BaseMapper<TransOrder> {

}




