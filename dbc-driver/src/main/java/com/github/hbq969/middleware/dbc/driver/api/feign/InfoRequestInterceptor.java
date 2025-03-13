package com.github.hbq969.middleware.dbc.driver.api.feign;

import com.google.gson.Gson;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InfoRequestInterceptor implements RequestInterceptor {

    private Gson gson = new Gson();

    @Override
    public void apply(RequestTemplate rt) {
        if (log.isDebugEnabled()) {
            log.debug("[{}] [{}], headers: {}, variables: {}, queries: {}, content-length: {}",
                    rt.method(), rt.path(), gson.toJson(rt.headers()),
                    gson.toJson(rt.getRequestVariables()), gson.toJson(rt.queries()),
                    rt.requestBody().length());
        }
    }
}
