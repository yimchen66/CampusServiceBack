package com.example.controller;

import com.example.common.BaseResponse;
import com.example.common.ResultUtils;
import com.example.model.domain.Carousel;
import com.example.service.CarouselService;
import com.example.utils.RedisUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: chenyim
 * @CreateTime: 2023-06-20  20:42
 * @Description: TODO
 */

@RestController
@RequestMapping("/main/carousel")
@Api(tags = "首页轮播图")
public class CarouselController {

    @Autowired
    private CarouselService carouselService;

    @Autowired
    private RedisUtil redisUtil;

    /**
     * @author chenyim
     * @date 2023/6/20 20:43
     * @Description  获取首页的轮播图
     * param
     * return
     */
    @ApiOperation("获取轮播图")
    @GetMapping()
    public BaseResponse<List<Carousel>> getCarousel(){
        //先检查缓存
        List<Object> carsouelList = redisUtil.lGet("carousel", 0, -1);

        //命中缓存
        if(carsouelList != null && carsouelList.size() != 0){
            System.out.println("命中缓存");
            List<Carousel> list = carsouelList.stream()
                    .map(o -> (Carousel) o)
                    .collect(Collectors.toList());
            return ResultUtils.success(list);
        }
        //查询数据库
        List<Carousel> carouselList = carouselService.list();
        //存入缓存
        carouselList.forEach(carousel -> redisUtil.lSet("carousel", carousel));
        System.out.println("查数据库");
        return ResultUtils.success(carouselList);
    }

    /**
     * @author chenyim
     * @date 2023/6/20 22:03
     * @Description  通过id删除某个轮播图
     */
    @ApiOperation("通过id删除某个轮播图")
    @DeleteMapping("/delete/one")
    public BaseResponse<String> deleteCarouselById(@RequestBody Carousel carousel){
        //更新缓存
        redisUtil.lRemove("carousel",1,carousel);
        //删除数据库
        carouselService.removeById(carousel.getId());
        return ResultUtils.success("删除成功");
    }

    /**
     * @author chenyim
     * @date 2023/6/20 22:03
     * @Description  增加一个轮播图
     */
    @ApiOperation("增加轮播图")
    @PostMapping("/add/one")
    public BaseResponse<String> addCarouselOne(@RequestBody Carousel carousel){
        //先加入数据库
        carouselService.save(carousel);
        //更新缓存
        redisUtil.lSet("carousel",carousel);
        return ResultUtils.success("添加成功！");
    }

}
