package com.github.hbq969.middleware.dbc.control;

import cn.hutool.core.lang.Pair;
import com.github.hbq969.code.common.encrypt.ext.config.Decrypt;
import com.github.hbq969.code.common.encrypt.ext.config.Encrypt;
import com.github.hbq969.middleware.dbc.model.APIModel;
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

    @ApiOperation("获取服务配置")
    @RequestMapping(path = "/config/list", method = RequestMethod.POST)
    @ResponseBody
    @Encrypt
    @Decrypt
    public List<Pair> getConfigList(@RequestBody APIModel model) {
        return apiService.getConfigList(model);
    }
}
