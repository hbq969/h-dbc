package com.github.hbq969.middleware.dbc.auth;

import com.github.hbq969.code.common.spring.interceptor.AbstractHandlerInterceptor;
import com.github.hbq969.middleware.dbc.config.Config;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Types;
import java.util.Base64;
import java.util.List;

@ConditionalOnProperty(prefix = "dbc.auth", name = "enabled", havingValue = "true")
@Component("dbc-HdbcSessionInterceptor")
@Slf4j
public class HdbcSessionInterceptor extends AbstractHandlerInterceptor {

    @Autowired
    private Config conf;

    @Autowired
    private JdbcTemplate jt;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public List<String> getPathPatterns() {
        return conf.getAuth().getBasic().getIncludeUrls();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String auth = request.getHeader(conf.getAuth().getBasic().getKey());
        if (StringUtils.isEmpty(auth)) {
            log.warn("basic认证，无认证信息");
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        } else {
            String decodedString = new String(Base64.getDecoder().decode(auth.substring("Basic ".length())));
            String[] array = decodedString.split(":");
            if (array.length != 2) {
                log.warn("basic认证格式有问题");
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                return false;
            } else {
                List<UserInfo> uis = jt.query("select username,password from h_users where username=?", new Object[]{array[0]}, new int[]{Types.VARCHAR}, (rs, rowNum) -> {
                    UserInfo ui = new UserInfo();
                    ui.setUsername(rs.getString(1));
                    ui.setPassword(rs.getString(2));
                    return ui;
                });
                if (CollectionUtils.isNotEmpty(uis)) {
                    boolean r = encoder.matches(array[1], uis.get(0).getPassword());
                    log.warn("basic认证结果，{}, {}", r, array[0]);
                    if (!r) {
                        response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    }
                    return r;
                } else {
                    log.warn("basic认证，账号不存在");
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    return false;
                }
            }
        }
    }

    @Override
    public int order() {
        return 3;
    }
}
