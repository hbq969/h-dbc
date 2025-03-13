package com.github.hbq969.middleware.dbc.auth;

import com.github.hbq969.code.common.spring.interceptor.AbstractHandlerInterceptor;
import com.github.hbq969.code.common.utils.GsonUtils;
import com.github.hbq969.code.sm.login.session.UserContext;
import com.github.hbq969.middleware.dbc.config.Config;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
@Component("dbc-APIAuthInterceptor")
@Slf4j
public class APIAuthInterceptor extends AbstractHandlerInterceptor {

    @Value("${spring.application.name:h-dbc}")
    private String app;

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
                String sql = "select username,password from h_users where app=? and username=?";
                Object[] args = new Object[]{app, array[0]};
                if (log.isDebugEnabled()) {
                    log.debug("查询账号信息: {}, {}", sql, GsonUtils.toJson(args));
                }
                List<UserInfo> uis = jt.query(sql, args, new int[]{Types.VARCHAR, Types.VARCHAR}, (rs, rowNum) -> {
                    UserInfo ui = new UserInfo();
                    ui.setUsername(rs.getString(1));
                    ui.setPassword(rs.getString(2));
                    return ui;
                });
                if (CollectionUtils.isNotEmpty(uis)) {
                    UserInfo ui = uis.get(0);
                    boolean r = encoder.matches(array[1], uis.get(0).getPassword());
                    log.info("basic认证结果，{}: {}", array[0], r ? "通过" : "不通过");
                    if (r) {
                        com.github.hbq969.code.sm.login.model.UserInfo su = new com.github.hbq969.code.sm.login.model.UserInfo();
                        su.setUserName(ui.getUsername());
                        UserContext.set(su);
                    } else {
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
