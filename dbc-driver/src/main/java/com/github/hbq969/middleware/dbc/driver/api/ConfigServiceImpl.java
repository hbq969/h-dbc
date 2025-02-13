package com.github.hbq969.middleware.dbc.driver.api;

import com.github.hbq969.code.common.spring.cloud.feign.FeignFactoryBean;

public class ConfigServiceImpl extends FeignFactoryBean<ConfigService> {

    private String apiSecret;
    private String apiCharset = "utf-8";

    public ConfigServiceImpl(String apiSecret, String apiCharset) {
        this.apiSecret = apiSecret;
        this.apiCharset = apiCharset;
    }

    @Override
    protected feign.codec.Encoder encoder() {
        return new Encoder(this.apiSecret, apiCharset);
    }

    @Override
    protected feign.codec.Decoder decoder() {
        return new Decoder(this.apiSecret, apiCharset);
    }
}
