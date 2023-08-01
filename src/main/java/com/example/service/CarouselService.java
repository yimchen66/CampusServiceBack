package com.example.service;

import com.example.model.domain.Carousel;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author cheny
* @description 针对表【carousel】的数据库操作Service
* @createDate 2023-06-20 11:11:06
*/
public interface CarouselService extends IService<Carousel> {
    List<Carousel> getAllCarousel();
}
