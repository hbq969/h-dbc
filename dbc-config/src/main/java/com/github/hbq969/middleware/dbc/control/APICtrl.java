package com.github.hbq969.middleware.dbc.control;

import cn.hutool.core.lang.Pair;
import com.github.hbq969.code.common.encrypt.ext.config.Decrypt;
import com.github.hbq969.code.common.encrypt.ext.config.Encrypt;
import com.github.hbq969.code.common.spring.advice.log.LogSet;
import com.github.hbq969.middleware.dbc.model.APIModel;
import com.github.hbq969.middleware.dbc.model.ConfigModel;
import com.github.hbq969.middleware.dbc.service.APIService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("dbc-APICtrl")
@Api(tags = "配置中心-api接口")
@RequestMapping(path = "/api")
@Slf4j
public class APICtrl {

    @Autowired
    private APIService apiService;

    @ApiOperation("获取服务配置（服务维度）")
    @RequestMapping(path = "/config/list", method = RequestMethod.POST)
    @ResponseBody
    @Encrypt
    @Decrypt
    @LogSet(printIn = false, printResult = false)
    public List<Pair> getConfigList(@RequestBody APIModel model) {
        return apiService.getConfigList(model);
    }

    @ApiOperation("获取服务配置（单个配置）")
    @RequestMapping(path = "/config/value", method = RequestMethod.POST)
    @Decrypt
    @Encrypt
    public Pair getConfigValue(@RequestBody ConfigModel model) {
        return apiService.getConfigValue(model);
    }
}
