package com.example.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: chenyim
 * @CreateTime: 2023-06-21  15:37
 * @Description: mybatisplus配置类
 */

@Configuration
public class MPConfig {
    @Bean
    public MybatisPlusInterceptor mpIntercepter(){
        //1、定义Mp拦截器
        MybatisPlusInterceptor mp = new MybatisPlusInterceptor();
        //2、添加分页拦截器
        mp.addInnerInterceptor(new PaginationInnerInterceptor());
        //3、添加乐观锁的拦截器
        mp.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return mp;
    }
}
