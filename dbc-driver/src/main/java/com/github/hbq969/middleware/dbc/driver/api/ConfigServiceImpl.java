package com.github.hbq969.middleware.dbc.driver.api;

import com.github.hbq969.middleware.dbc.driver.api.feign.FeignFactoryBean;
import com.github.hbq969.middleware.dbc.driver.config.ApiInfo;
import feign.Client;
import feign.Request;
import feign.RequestInterceptor;
import feign.Retryer;
import feign.httpclient.ApacheHttpClient;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;

import javax.net.ssl.SSLContext;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.LinkedList;

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
        ins.addFirst(new BasicAuthRequestInterceptor(api));
        return ins;
    }

    @Override
    protected Client client() {
        if (!api.isHttps())
            return super.client();

        try {
            // 1. 安全加载证书
            KeyStore trustStore = KeyStore.getInstance("PKCS12");
            try (InputStream is = getClass().getResourceAsStream("/certs/hdbc_client.p12")) {
                if (is == null)
                    throw new FileNotFoundException("hdbc_client.p12 not found");
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
