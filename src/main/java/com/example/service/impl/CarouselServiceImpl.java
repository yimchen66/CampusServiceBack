package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.model.domain.Carousel;
import com.example.service.CarouselService;
import com.example.mapper.CarouselMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author cheny
* @description 针对表【carousel】的数据库操作Service实现
* @createDate 2023-06-20 11:11:06
*/
@Service
public class CarouselServiceImpl extends ServiceImpl<CarouselMapper, Carousel>
    implements CarouselService{

    /**
     * @author chenyim
     * @date 2023/6/20 21:19
     * @Description  获取所有的轮播图信息
     */
    @Override
    public List<Carousel> getAllCarousel() {

        return null;
    }
}




