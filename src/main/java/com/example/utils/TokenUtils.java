package com.example.utils;

import cn.hutool.core.date.DateUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import java.util.Date;

/**
 * @Author: chenyim
 * @CreateTime: 2023-06-21  08:56
 * @Description: token验证
 */
public class TokenUtils {
    public static String getToken(String userId,String sign){   //以password作为签名
        return JWT.create().withAudience(userId) // 将 user id 保存到 token 里面.作为载荷
                .withExpiresAt(DateUtil.offsetHour(new Date(),7*24)) //小时
                .sign(Algorithm.HMAC256(sign)); // 以 password 作为 token 的密钥
    }
}

//35013913 胡