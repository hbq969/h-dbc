package com.github.hbq969.middleware.dbc.driver.api;

import cn.hutool.core.lang.Pair;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

public interface ConfigService {
    @RequestMapping(path = "/config/list",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            method = RequestMethod.POST)
    @ResponseBody
    List<Pair> getConfigList(@RequestBody ConfigInfo model);
}
