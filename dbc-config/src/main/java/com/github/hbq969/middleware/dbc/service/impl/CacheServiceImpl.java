package com.github.hbq969.middleware.dbc.service.impl;

import com.github.hbq969.code.common.cache.ExpireKey;
import com.github.hbq969.middleware.dbc.model.APIModel;
import com.github.hbq969.middleware.dbc.service.CacheService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("dbc-CacheServiceImpl")
@Slf4j
public class CacheServiceImpl implements CacheService {

    @Autowired
    private CacheManager manager;

    private final static List<String> formats = Lists.newArrayList("PROP", "YAML");

    @Override
    public void clear(APIModel model) {
        for (String format : formats) {
            model.setType(APIModel.ConfigType.valueOf(format));
            String key = model.key();
            if (log.isDebugEnabled())
                log.debug("配置数据发生变化, 刷新接口缓存: {}", key);
            Cache c = manager.getCache("default");
            if (c != null)
                c.evict(ExpireKey.of(key));
        }
    }
}
