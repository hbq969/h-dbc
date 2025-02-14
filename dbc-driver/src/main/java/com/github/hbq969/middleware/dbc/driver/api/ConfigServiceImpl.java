package com.github.hbq969.middleware.dbc.driver.api;

import com.github.hbq969.code.common.spring.cloud.feign.FeignFactoryBean;
import com.github.hbq969.middleware.dbc.driver.config.ApiInfo;
import feign.Retryer;

import java.util.concurrent.TimeUnit;

public class ConfigServiceImpl extends FeignFactoryBean<ConfigService> {

    private ApiInfo api;

    public ConfigServiceImpl(ApiInfo api) {
        this.api = api;
    }

    @Override
    protected feign.codec.Encoder encoder() {
        return new Encoder(this.api.getSecret(), this.api.getCharset());
    }

    @Override
    protected feign.codec.Decoder decoder() {
        return new Decoder(this.api.getSecret(), this.api.getCharset());
    }

    @Override
    protected Retryer feignRetry() {
        return new Retryer.Default(1000L, TimeUnit.SECONDS.toMillis(5L), this.api.getRetry());
    }
}
