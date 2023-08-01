package com.example.controller;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.example.common.BaseResponse;
import com.example.common.ErrorCode;
import com.example.common.ResultUtils;
import com.example.exception.BusinessException;
import com.example.model.domain.Orders;
import com.example.model.dto.DetailedOrderDTO;
import com.example.model.request.order.CreateOrderRequest;
import com.example.model.request.order.PageOrderRequest;
import com.example.model.request.order.UpdateOrderRequest;
import com.example.service.OrdersService;
import com.example.utils.RedisUtil;
import com.sun.org.apache.xpath.internal.operations.Or;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.util.ClassUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @Author: chenyim
 * @CreateTime: 2023-06-21  15:11
 * @Description: 订单管理
 */

@RestController
@RequestMapping("/order")
@Api(tags = "订单管理")
public class OrderController {

    @Autowired
    private OrdersService ordersService;

    @Autowired
    private RedisUtil redisUtil;
    @Value("${global.rankSize}")
    private int rankSize;
    @Value("${global.ip}")
    private String IP;
    @Value("${server.port}")
    private String PORT;

    /**
     * @author chenyim
     * @date 2023/6/21 15:13
     * @Description  获取所有订单（包括已经被接单，不包括已经被删除的）
     *              默认顺序：订单创建时间
     */
    @ApiOperation("获取分页订单，所有")
    @PostMapping("/pageorder")
    public BaseResponse<List<Orders>> getAllOrders(@RequestBody PageOrderRequest pageOrderRequest){
        int pageNumber = pageOrderRequest.getPageNumber();
        int pages = pageOrderRequest.getPages();
        //参数为空处理
        if(StrUtil.isBlank(String.valueOf(pages)) || StrUtil.isBlank(String.valueOf(pageNumber)) )
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数不能为空");

        String column = pageOrderRequest.getColumn();
        String sortedType = pageOrderRequest.getSortedType();
        List<Orders> pagedOrderList = ordersService.getPagedOrderList(pages, pageNumber, "包括",
                    column, sortedType);
        return ResultUtils.success(pagedOrderList);
    }

    /**
     * @author chenyim
     * @date 2023/6/21 15:13
     * @Description  获取所有订单（不包括已经被接单和已经被删除的）
     *              默认顺序：订单创建时间
     */
    @ApiOperation("获取分页订单，未被接单的")
    @PostMapping("/pagenoorder")
    public BaseResponse<List<Orders>> getAvailableOrders(@RequestBody PageOrderRequest pageOrderRequest){
        int pageNumber = pageOrderRequest.getPageNumber();
        int pages = pageOrderRequest.getPages();
        //参数为空处理
        if(StrUtil.isBlank(String.valueOf(pages)) || StrUtil.isBlank(String.valueOf(pageNumber)) )
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数不能为空");

        String column = pageOrderRequest.getColumn();
        String sortedType = pageOrderRequest.getSortedType();
        List<Orders> pagedOrderList = ordersService.getPagedOrderList(pages, pageNumber, "不包括",
                column, sortedType);
        return ResultUtils.success(pagedOrderList);
    }

    /**
     * @author chenyim
     * @date 2023/6/21 16:42
     * @Description  获取某个订单详情
     */
    @ApiOperation("管理员获取某个订单详情")
    @GetMapping("/get/one/{id}")
    public BaseResponse<DetailedOrderDTO> getDetailedOrderByAdmin(@PathVariable String id){
        if (id == null || id.equals(""))
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数不能为空");
        DetailedOrderDTO detailedOrder = ordersService.getDetailedOrderByAdmin(id);
        return ResultUtils.success(detailedOrder);
    }

    /**
     * @author chenyim
     * @date 2023/6/22 22:00
     * @Description  用户获取某个订单详情
     * param
     * return
     */
    @ApiOperation("用户获取某个订单详情")
    @GetMapping("/get/one/user/{id}")
    public BaseResponse<DetailedOrderDTO> getDetailedOrderByUser(@PathVariable String id){
        if (id == null || id.equals(""))
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数不能为空");

        DetailedOrderDTO detailedOrder = ordersService.getDetailedOrderByUser(id);
        //将这个订单的热度加1，普通榜
        redisUtil.zIncrement("order_hot",id,1);
        double curOrderHot =(double) redisUtil.zGetScore("order_hot", id);

        //热度榜,将orders转为json，作为id
        Orders orders = detailedOrder.getOrders();
        Orders newOrder = new Orders();
        BeanUtils.copyProperties(orders,newOrder);
        newOrder.setVersion(null);
        String rankId = JSON.toJSONString(newOrder);
        //先查看当前订单在不在热度榜内,如果已经再了，则直接增加分数
        if( redisUtil.zGetScore("order_ranking", rankId) != null){
            redisUtil.zIncrement("order_ranking",rankId,1);
        }else{//不在榜内
            //如果热度榜不够，则直接入榜
            if(redisUtil.zGetSize("order_ranking") < rankSize)
                redisUtil.zIncrement("order_ranking", rankId,curOrderHot);
            else{
                //如果已经超过rankSize,比较最后一名的热度值
                Set<ZSetOperations.TypedTuple<Object>> resultSet =
                        redisUtil.zGetByIndexReserve("order_ranking", rankSize - 1);
                String lastId = "";
                double lastScore = 0;
                for (ZSetOperations.TypedTuple<Object> tuple : resultSet) {
                    lastId = (String) tuple.getValue();
                    lastScore = tuple.getScore();
                }
                //当前订单热度值
                //如果大于最后一名的热度,则将最后一个去掉，并将当前的rankId和热度加入热度榜
                if(curOrderHot > lastScore){
                    redisUtil.zRemove("order_ranking", lastId);
                    redisUtil.zIncrement("order_ranking", rankId, curOrderHot);
                }
            }
        }
        Set<ZSetOperations.TypedTuple<Object>> order_hot = redisUtil.zGetRangeReverse("order_hot", 0, -1);
        for (ZSetOperations.TypedTuple<Object> tuple : order_hot) {
            String member =(String) tuple.getValue();
            double score = tuple.getScore();
            System.out.println("订单: " + member + ", 分数: " + score);
        }
        System.out.println("-------------热度榜---------------------------");
        Set<ZSetOperations.TypedTuple<Object>> order_rank = redisUtil.zGetRangeReverse("order_ranking", 0, -1);
        for (ZSetOperations.TypedTuple<Object> tuple : order_rank) {
            String member =(String) tuple.getValue();
            double score = tuple.getScore();
            System.out.println("订单: " + member + ", 分数: " + score);
        }
        return ResultUtils.success(detailedOrder);
    }

    /**
     * @author chenyim
     * @date 2023/6/27 0:40
     * @Description  获取订单热度
     * param
     * return
     */
    @ApiOperation("获取订单热度")
    @GetMapping("/get/hot/{orderId}")
    public BaseResponse<Double> getOrderHot(@PathVariable String orderId){
        Double result = Double.valueOf(0);
        //查缓存
        Object order_hot = redisUtil.zGetScore("order_hot", orderId);
        if(order_hot != null)
            result = (Double) order_hot;

        return ResultUtils.success(result);
    }

    /**
     * @author chenyim
     * @date 2023/6/27 13:46
     * @Description  获取排行榜的订单
     */
    @ApiOperation("获取排行榜的订单")
    @PostMapping("/get/ranking")
    public BaseResponse<List<Orders>> getRankingOrders(@RequestBody PageOrderRequest pageOrderRequest){
        int pageNumber = pageOrderRequest.getPageNumber();//每一页的个数
        int pages = pageOrderRequest.getPages();//第几页
        Set<ZSetOperations.TypedTuple<Object>> orderRanking = redisUtil.zGetRangeReverse("order_ranking",
                (long) (pages - 1) * pageNumber,
                (long) pages * pageNumber - 1);
        if(orderRanking == null){
            System.out.println("为空");
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"数据不存在");
        }
        List<Orders> list = new ArrayList<>();
        for (ZSetOperations.TypedTuple<Object> tuple : orderRanking) {
            String member = (String) tuple.getValue();
            Orders orders = JSON.parseObject(member, Orders.class);
            double score = tuple.getScore();
            orders.setOrderHot((long) score);
            list.add(orders);
        }
        return ResultUtils.success(list);
    }


    /**
     * @author chenyim
     * @date 2023/6/21 19:57
     * @Description  用户接受某个订单
     */
    @ApiOperation("用户接受某个订单")
    @PostMapping("/accept")
    public BaseResponse<String> acceptOrder(@RequestBody DetailedOrderDTO detailedOrderDTO){
        ordersService.acceptOrder(detailedOrderDTO);
        return ResultUtils.success("接受成功！");
    }

    /**
     * @author chenyim
     * @date 2023/6/22 11:00
     * @Description  用户创造某个订单
     */
    @ApiOperation("用户创造某个订单")
    @PostMapping("/create")
    public BaseResponse<Orders> createOrder(@RequestBody CreateOrderRequest createOrderRequest){
        Orders orders = ordersService.createOrderByUser(createOrderRequest);
        return ResultUtils.success(orders);
    }

    /**
     * @author chenyim
     * @date 2023/6/22 11:00
     * @Description  编辑某个订单
     */
    @ApiOperation("用户编辑某个订单")
    @PostMapping("/update")
    public BaseResponse<Orders> updateOrder(@RequestBody UpdateOrderRequest updateOrderRequest){
        Orders orders = ordersService.updateOrderByUser(updateOrderRequest);
        return ResultUtils.success(orders);
    }

    /**
     * @author chenyim
     * @date 2023/6/23 14:08
     * @Description  上传图片
     */
    @ApiOperation("上传图片")
    @PostMapping("/upload")
    public BaseResponse<String> uploadImage(@RequestParam("image") MultipartFile image) {
        // 获取上传的文件信息
        String originalFilename = image.getOriginalFilename();
        String contentType = image.getContentType();
        //判空
        if(StrUtil.isBlank(originalFilename) || StrUtil.isBlank(contentType))
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求参数错误");
        //获取图片类型
        String picType = originalFilename.split("\\.")[1];

        //获取jar包所在目录
        ApplicationHome h = new ApplicationHome(getClass());
        File jarF = h.getSource();
        //在jar包所在目录下生成一个upload文件夹用来存储上传的图片
        String dirPath = jarF.getParentFile().toString()+"/pictures/";
        File filePath=new File(dirPath);
        if(!filePath.exists()){
            filePath.mkdirs();
        }

        try {
            //雪花算法生成图片id
            String picName = IdUtil.simpleUUID() + "."+picType;
            image.transferTo(new File(dirPath+"/"+picName));

            String picUrl = "http://"+IP+":"+PORT+"/pictures/"+picName;
            return ResultUtils.success(picUrl);
        } catch (IOException e) {
            e.printStackTrace();
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"上传失败 "+e);
        }
    }

    /**
     * @author chenyim
     * @date 2023/6/28 10:36
     * @Description  获取某个用户正在交易中的订单
     */
    @ApiOperation("获取某个用户正在交易中的订单")
    @GetMapping("/get/user/trans/{userID}")
    public BaseResponse<List<DetailedOrderDTO>> getUserTeans(@PathVariable String userID ){
        if(StrUtil.isBlank(userID))
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        List<DetailedOrderDTO> userTrans = ordersService.getUserTrans(userID);
        return ResultUtils.success(userTrans);
    }

    /**
     * @author chenyim
     * @date 2023/6/28 10:36
     * @Description  获取某个用户已经完成的订单
     */
    @ApiOperation("获取某个用户已经完成的订单")
    @GetMapping("/get/user/finish/{userID}")
    public BaseResponse<List<DetailedOrderDTO>> getUserFinish(@PathVariable String userID ){
        if(StrUtil.isBlank(userID))
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        List<DetailedOrderDTO> userTrans = ordersService.getUserFinish(userID);
        return ResultUtils.success(userTrans);
    }

    /**
     * @author chenyim
     * @date 2023/6/28 14:10
     * @Description  用户删除某个未被接单的订单
     */
    @ApiOperation("用户删除某个未被接单的订单")
    @DeleteMapping("/delete/unreceive/{id}")
    public BaseResponse<String> userDeleteOrder(@PathVariable String id){
        if(StrUtil.isBlank(id))
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求参数错误");
        boolean b = ordersService.removeById(id);
        return ResultUtils.success(b?"删除成功":"删除失败");
    }

}
