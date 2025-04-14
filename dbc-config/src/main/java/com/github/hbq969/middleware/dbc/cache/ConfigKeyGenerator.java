package com.github.hbq969.middleware.dbc.cache;

import cn.hutool.core.util.ArrayUtil;
import com.github.hbq969.code.common.cache.Expire;
import com.github.hbq969.code.common.cache.ExpireKey;
import com.github.hbq969.code.common.utils.FormatTime;
import com.github.hbq969.middleware.dbc.model.APIModel;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component("configKeyGenerator")
public class ConfigKeyGenerator extends SimpleKeyGenerator {
    @Override
    public Object generate(Object target, Method method, Object... params) {
        if (ArrayUtil.isEmpty(params))
            throw new IllegalArgumentException("拉取配置接口必须要传入APIModel对象，请检查");
        APIModel q = (APIModel) params[0];
        if (q == null)
            throw new IllegalArgumentException("拉取配置接口传入APIModel对象为空，请检查");
        String key = q.key();
        Expire expire = AnnotationUtils.findAnnotation(method, Expire.class);
        if (expire == null)
            throw new UnsupportedOperationException(String.format("接口<%s>缓存必须标注@Expire注解", method.getName()));
        ExpireKey ek = new ExpireKey(key, expire.time(), expire.unit(), FormatTime.nowMills());
        return ek;
    }
}
