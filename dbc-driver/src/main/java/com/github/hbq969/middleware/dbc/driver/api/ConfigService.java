package com.github.hbq969.middleware.dbc.driver.api;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

public interface ConfigService {

    @RequestMapping(path = "/api/config/list",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            method = RequestMethod.POST)
    @ResponseBody
    Object getConfigList(@RequestBody ConfigInfo model);


}
