package com.example.config.interceptor;

import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;

import com.example.common.ErrorCode;
import com.example.exception.BusinessException;
import com.example.model.domain.User;
import com.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
/**
 * @Author: chenyim
 * @CreateTime: 2023-06-21  09:32
 * @Description: token拦截器
 */
@Component
public class JwtInterceptor extends InterceptorRegistry implements HandlerInterceptor {
    @Autowired
    private UserService userService;


    public boolean preHandle(HttpServletRequest httpServletRequest,
                             HttpServletResponse httpServletResponse, Object object) throws Exception{

        //获取token
        String token = httpServletRequest.getHeader("token");
        // 如果不是映射到方法直接通过
        if(!(object instanceof HandlerMethod)){
            return true;
        }
        //判断是不是swagger是就放行
        HandlerMethod handlerMethod=(HandlerMethod)object;
        if("springfox.documentation.swagger.web.ApiResourceController".equals(handlerMethod.getBean().getClass().getName())){
            return  true;
        }

        // 执行认证
        if (StrUtil.isBlank(token)) {
            throw new BusinessException(ErrorCode.TOKEN_DEFECT, "Token缺失");
        }

        //获取token的userid
        String userId;
        try {
            //解密获取
            userId = JWT.decode(token).getAudience().get(0); //得到token中的userid载荷
        } catch (JWTDecodeException j) {
            throw new BusinessException(ErrorCode.TOKEN_ILLEGAL,"token验证失败，重新登陆");
        }

        //根据userid查询数据库
        User user = userService.getById(userId);

        if(user == null){
            throw new BusinessException(ErrorCode.TOKEN_ILLEGAL,"token验证失败，重新登陆");
        }

        // 用户密码加签验证 token
        JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(user.getPassword())).build();
        try {
            jwtVerifier.verify(token);
        } catch (JWTVerificationException e) {
            throw new BusinessException(ErrorCode.TOKEN_ILLEGAL,"token过期，重新登陆");
        }
        return true;
    }
}


