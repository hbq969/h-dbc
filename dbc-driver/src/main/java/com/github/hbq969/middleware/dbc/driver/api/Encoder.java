package com.github.hbq969.middleware.dbc.driver.api;

import cn.hutool.core.map.MapUtil;
import com.github.hbq969.middleware.dbc.driver.config.ApiInfo;
import com.github.hbq969.middleware.dbc.driver.utils.AESUtils;
import com.github.hbq969.middleware.dbc.driver.utils.Base64Util;
import com.github.hbq969.middleware.dbc.driver.utils.RSAUtil;
import com.google.gson.Gson;
import feign.RequestTemplate;
import feign.codec.EncodeException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class Encoder implements feign.codec.Encoder {

    private Gson gson = new Gson();
    private ApiInfo api;
    private PublicKeyService publicKeyService;

    public Encoder(ApiInfo api) {
        this.api = api;
        FactoryBean<PublicKeyService> fb = new PublicKeyServiceImpl(api);
        try {
            this.publicKeyService = fb.getObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void encode(Object object, Type bodyType, RequestTemplate template) throws EncodeException {
        Map pubInfo = this.publicKeyService.getPublicKey();
        if (MapUtil.isEmpty(pubInfo)) {
            throw new RuntimeException("请求公钥未空，请检查");
        }
        String publicKey = MapUtil.getStr(pubInfo, "body");
        if (publicKey == null || publicKey.length() == 0) {
            throw new RuntimeException("返回的公钥未空，请检查");
        }
        String key = AESUtils.randomKeyWithAES(api.getLengthAesKey());
        String iv = AESUtils.randomKeyWithAES(api.getLengthAesKey());
        MDC.put("dbc,api,aes,key", key);
        MDC.put("dbc,api,aes,iv", iv);
        String content = gson.toJson(object);
        if (api.isApiLog()) {
            log.debug("加密前, 公钥: {}, 对称加密key: {}, 对称加密iv: {}, 报文: {}", publicKey, key, iv, content);
        }
        String encryptBody = AESUtils.encrypt(content, key, iv, Charset.forName(this.api.getCharset()));
        Map map = new HashMap();
        map.put("body", encryptBody);
        try {
            byte[] buf = RSAUtil.encrypt(key.getBytes(this.api.getCharset()), publicKey);
            map.put("key", Base64Util.encode(buf));
            buf = RSAUtil.encrypt(iv.getBytes(this.api.getCharset()), publicKey);
            map.put("iv", Base64Util.encode(buf));
        } catch (Exception e) {
            log.error("加密key、iv异常", e);
            throw new RuntimeException("加密key、iv异常", e.getCause());
        }
        String body = gson.toJson(map);
        if (api.isApiLog()) {
            log.debug("加密后: {}", body);
        }
        template.body(body);
    }
}
