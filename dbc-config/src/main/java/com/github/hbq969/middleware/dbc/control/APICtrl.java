package com.github.hbq969.middleware.dbc.control;

import com.github.hbq969.code.common.config.EncryptProperties;
import com.github.hbq969.code.common.encrypt.ext.config.Algorithm;
import com.github.hbq969.code.common.encrypt.ext.config.Decrypt;
import com.github.hbq969.code.common.encrypt.ext.config.Encrypt;
import com.github.hbq969.code.common.restful.ReturnMessage;
import com.github.hbq969.code.common.spring.advice.limit.RestfulLimit;
import com.github.hbq969.code.common.spring.advice.log.LogSet;
import com.github.hbq969.code.common.spring.yaml.TypePair;
import com.github.hbq969.middleware.dbc.model.APIModel;
import com.github.hbq969.middleware.dbc.model.ConfigModel;
import com.github.hbq969.middleware.dbc.service.APIService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

@RestController("dbc-APICtrl")
@Api(tags = "配置中心-api接口")
@RequestMapping(path = "/api")
@Slf4j
public class APICtrl {

    @Autowired
    private APIService apiService;

    @Autowired(required = false)
    private EncryptProperties encryptConfig;

    @ApiOperation("获取rsa公钥")
    @RequestMapping(path = "/publicKey", method = RequestMethod.GET)
    @ResponseBody
    @RestfulLimit(forSameIP = true, value = 1)
    public ReturnMessage<?> getPublicKey() {
        return ReturnMessage.success(encryptConfig == null ? null
                : encryptConfig.getRestful().getRsa().getPublicKey());
    }

    @ApiOperation("获取服务配置（服务维度）")
    @RequestMapping(path = "/config/list", method = RequestMethod.POST)
    @ResponseBody
    @Encrypt
    @Decrypt(algorithm = Algorithm.RAS)
    @LogSet(printIn = false, printResult = false)
    // 限制10秒一次的请求
    @RestfulLimit(forSameIP = true, value = 6, unit = TimeUnit.MINUTES)
    public Object getConfigList(@RequestBody APIModel model) {
        return apiService.getConfigList(model);
    }

    @ApiOperation("获取服务配置（单个配置）")
    @RequestMapping(path = "/config/value", method = RequestMethod.POST)
    @Encrypt
    @Decrypt(algorithm = Algorithm.RAS)
    @RestfulLimit(forSameIP = true, value = 5)
    public TypePair getConfigValue(@RequestBody ConfigModel model) {
        return apiService.getConfigValue(model);
    }
}
