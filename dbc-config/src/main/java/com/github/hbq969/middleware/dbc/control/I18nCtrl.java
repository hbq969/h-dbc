package com.github.hbq969.middleware.dbc.control;

import com.github.hbq969.code.common.restful.ReturnMessage;
import com.github.hbq969.code.common.spring.context.SpringContext;
import com.github.hbq969.code.dict.service.api.impl.MapDictHelperImpl;
import com.github.hbq969.code.sm.login.utils.I18nUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController("dbc-i18nCtrl")
@RequestMapping(path = "/dbc-ui/i18n")
@Slf4j
@Tag(name = "页面使用-国际化管理")
public class I18nCtrl {

    @Autowired
    private com.github.hbq969.code.common.spring.i18n.I18nCtrl i18nCtrl;

    @Autowired
    private MapDictHelperImpl dict;

    @Value("${spring.application.name}")
    private String app;

    @Autowired
    private SpringContext context;

    @Operation(summary ="设置语言")
    @RequestMapping(path = "/lang", method = RequestMethod.PUT)
    @ResponseBody
    public ReturnMessage<String> langSet(@RequestBody Map map) {
        return i18nCtrl.langSet(map);
    }

    @Operation(summary ="获取语言")
    @RequestMapping(path = "/lang", method = RequestMethod.GET)
    @ResponseBody
    public ReturnMessage<String> getLang() {
        return ReturnMessage.success(I18nUtils.getFullLanguage(context));
    }

    @Operation(summary ="获取语言数据")
    @RequestMapping(path = "/lang/data", method = RequestMethod.GET)
    @ResponseBody
    public ReturnMessage<Map<String, String>> getLangData() {
        Map<String, String> data = new HashMap<>();
        String key = I18nUtils.getFullLanguage(context);
        log.info("查询 {} 数据", key);
        Map<String, String> pairs = dict.queryPairs(key);
        if (pairs != null) {
            data.putAll(pairs);
        }
        key = String.join("", app, ",", "lang", ",", I18nUtils.getFullLanguage(context));
        log.info("查询 {} 数据", key);
        pairs = dict.queryPairs(key);
        if (pairs != null) {
            data.putAll(pairs);
        }
        return ReturnMessage.success(data);
    }
}
