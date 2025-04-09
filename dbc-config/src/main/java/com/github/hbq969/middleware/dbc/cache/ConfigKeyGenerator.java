package com.github.hbq969.middleware.dbc.cache;

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
        APIModel q = (APIModel) params[0];
        String key = q.key();
        Expire expire = AnnotationUtils.findAnnotation(method, Expire.class);
        ExpireKey ek = new ExpireKey(key, expire.time(), expire.unit(), FormatTime.nowMills());
        return ek;
    }
}
