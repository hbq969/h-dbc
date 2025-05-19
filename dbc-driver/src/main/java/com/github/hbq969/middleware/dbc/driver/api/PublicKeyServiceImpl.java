package com.github.hbq969.middleware.dbc.driver.api;

import com.github.hbq969.middleware.dbc.driver.api.feign.FeignFactoryBean;
import com.github.hbq969.middleware.dbc.driver.config.ApiInfo;
import feign.Client;
import feign.RequestInterceptor;
import feign.httpclient.ApacheHttpClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;

import javax.net.ssl.SSLContext;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.LinkedList;

@Slf4j
public class PublicKeyServiceImpl extends FeignFactoryBean<PublicKeyService> {
    private ApiInfo api;

    public PublicKeyServiceImpl(ApiInfo api) {
        this.api = api;
    }

    @Override
    public Class<PublicKeyService> getObjectType() {
        return PublicKeyService.class;
    }

    @Override
    public String getUrl() {
        return this.api.getUrl();
    }

    @Override
    protected LinkedList<RequestInterceptor> interceptors() {
        LinkedList<RequestInterceptor> ins = super.interceptors();
        if (!api.isApiLog()) {
            ins.clear();
        }
        ins.addFirst(new BasicAuthRequestInterceptor(api));
        return ins;
    }

    @Override
    protected Client client() {
        if (!api.isHttps())
            return super.client();

        try {
            // 1. 安全加载证书
            KeyStore trustStore = KeyStore.getInstance(this.api.getTruststoreType());
            try (InputStream is = getClass().getResourceAsStream(this.api.getTruststorePath())) {
                if (is == null)
                    throw new FileNotFoundException(String.format("%s not found", this.api.getTruststorePath()));
                trustStore.load(is, this.api.getTruststorePassword().toCharArray());
            }
            // 2. 构建SSL上下文
            SSLContext sslContext = SSLContextBuilder.create()
                    .loadTrustMaterial(trustStore, null).build();
            // 3. 构建安全客户端
            return new ApacheHttpClient(HttpClientBuilder.create()
                    .setSSLContext(sslContext)
                    .setSSLHostnameVerifier(new NoopHostnameVerifier()).build());
        } catch (Exception e) {
            throw new RuntimeException("HTTPS客户端初始化失败", e);
        }
    }
}
