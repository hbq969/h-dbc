package com.github.hbq969.middleware.dbc.driver.api;

import com.github.hbq969.middleware.dbc.driver.api.feign.FeignFactoryBean;
import com.github.hbq969.middleware.dbc.driver.config.ApiInfo;
import feign.Request;
import feign.RequestInterceptor;
import feign.Retryer;

import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

public class ConfigServiceImpl extends FeignFactoryBean<ConfigService> {

    private ApiInfo api;

    public ConfigServiceImpl(ApiInfo api) {
        this.api = api;
    }

    @Override
    protected feign.codec.Encoder encoder() {
        return new Encoder(this.api);
    }

    @Override
    protected feign.codec.Decoder decoder() {
        return new Decoder(this.api);
    }

    @Override
    protected Retryer feignRetry() {
        return new Retryer.Default(api.getRetryPeriodMills(), api.getRetryTimeoutMills(), this.api.getRetry());
    }

    @Override
    protected Request.Options options() {
        return new Request.Options((int) api.getConnectTimeoutMills(), 60000, true);
    }

    @Override
    protected LinkedList<RequestInterceptor> interceptors() {
        LinkedList<RequestInterceptor> ins = super.interceptors();
        if (!api.isApiLog()) {
            ins.clear();
        }
        if (api.getAuth().isEnabled()) {
            ins.addFirst(new BasicAuthRequestInterceptor(api));
        }
        return ins;
    }
}
