package com.example.controller;

import com.example.common.BaseResponse;
import com.example.common.ResultUtils;
import com.example.model.domain.Carousel;
import com.example.model.domain.NavigateAct;
import com.example.service.CarouselService;
import com.example.service.NavigateActService;
import com.example.utils.RedisUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
@RequestMapping("/main/act")
@Api(tags = "首页活动")
public class NavigateActController {

    @Autowired
    private NavigateActService navigateActService;

    @Autowired
    private RedisUtil redisUtil;

    /**
     * @author chenyim
     * @date 2023/6/20 20:43
     * @Description  获取首页的所有活动
     */
    @ApiOperation("获取所有活动")
    @GetMapping()
    public BaseResponse<List<NavigateAct>> getAllActs(){
        //先检查缓存
        List<Object> navigateActlList = redisUtil.lGet("navigateact", 0, -1);

        //命中缓存
        if(navigateActlList != null && navigateActlList.size() != 0){
            System.out.println("命中缓存");
            List<NavigateAct> list = navigateActlList.stream()
                    .map(o -> (NavigateAct) o)
                    .collect(Collectors.toList());
            return ResultUtils.success(list);
        }
        //查询数据库
        List<NavigateAct> carouselList = navigateActService.list();
        //存入缓存
        carouselList.forEach(carousel -> redisUtil.lSet("navigateact", carousel));
        System.out.println("查数据库");
        return ResultUtils.success(carouselList);
    }

    /**
     * @author chenyim
     * @date 2023/6/20 22:03
     * @Description  通过id删除某个活动
     */
    @ApiOperation("通过id删除某个活动")
    @DeleteMapping("/delete/one")
    public BaseResponse<String> deleteActById(@RequestBody NavigateAct navigateAct){
        //更新缓存
        redisUtil.lRemove("navigateact",1,navigateAct);
        //删除数据库
        navigateActService.removeById(navigateAct.getId());
        return ResultUtils.success("删除成功");
    }

    /**
     * @author chenyim
     * @date 2023/6/20 22:03
     * @Description  增加一个活动
     */
    @ApiOperation("增加活动")
    @PostMapping("/add/one")
    public BaseResponse<String> addActOne(@RequestBody NavigateAct navigateAct){
        //先加入数据库
        navigateActService.save(navigateAct);
        //更新缓存
        redisUtil.lSet("navigateact",navigateAct);
        return ResultUtils.success("添加成功！");
    }

}
