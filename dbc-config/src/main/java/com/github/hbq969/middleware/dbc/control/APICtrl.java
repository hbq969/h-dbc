package com.github.hbq969.middleware.dbc.control;

import com.github.hbq969.code.common.cache.Expire;
import com.github.hbq969.code.common.config.EncryptProperties;
import com.github.hbq969.code.common.encrypt.ext.config.Algorithm;
import com.github.hbq969.code.common.encrypt.ext.config.Decrypt;
import com.github.hbq969.code.common.encrypt.ext.config.Encrypt;
import com.github.hbq969.code.common.restful.ReturnMessage;
import com.github.hbq969.code.common.spring.advice.log.LogSet;
import com.github.hbq969.middleware.dbc.model.APIModel;
import com.github.hbq969.middleware.dbc.service.APIService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

@RestController("dbc-APICtrl")
@Tag(name = "配置中心-api接口")
@RequestMapping(path = "/api")
@Slf4j
public class APICtrl {

    @Autowired
    private APIService apiService;

    @Autowired(required = false)
    private EncryptProperties encryptConfig;

    @Operation(summary = "获取rsa公钥")
    @RequestMapping(path = "/publicKey", method = RequestMethod.GET)
    @ResponseBody
    public ReturnMessage<?> getPublicKey() {
        return ReturnMessage.success(encryptConfig == null ? null
                : encryptConfig.getRestful().getRsa().getPublicKey());
    }

    @Operation(summary ="获取服务配置（服务维度）")
    @RequestMapping(path = "/config/list", method = RequestMethod.POST)
    @ResponseBody
    @Encrypt(algorithm = Algorithm.RAS)
    @Decrypt(algorithm = Algorithm.RAS)
    @LogSet(printIn = false, printResult = false)
    @Cacheable(keyGenerator = "configKeyGenerator", value = "default", unless = "#result==null")
    @Expire(methodKey = "getConfigList", time = 1, unit = TimeUnit.DAYS)
    public Object getConfigList(@RequestBody APIModel model) {
        return apiService.getConfigList(model);
    }

    @RequestMapping(path = "/config/list", method = RequestMethod.DELETE)
    @Cacheable(keyGenerator = "configKeyGenerator", value = "default", unless = "#result==null")
    @Expire(methodKey = "getConfigList", time = 10, unit = TimeUnit.SECONDS)
    @CacheEvict(keyGenerator = "configKeyGenerator", value = "default")
    public void clearConfigList(@RequestBody APIModel model) {
    }
}
