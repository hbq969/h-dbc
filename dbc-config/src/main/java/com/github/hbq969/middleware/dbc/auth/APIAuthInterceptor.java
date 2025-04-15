package com.github.hbq969.middleware.dbc.auth;

import com.github.hbq969.code.common.spring.interceptor.AbstractHandlerInterceptor;
import com.github.hbq969.code.common.utils.GsonUtils;
import com.github.hbq969.code.sm.login.event.SMUserEvent;
import com.github.hbq969.code.sm.login.session.UserContext;
import com.github.hbq969.middleware.dbc.config.Config;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationListener;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Types;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ConditionalOnProperty(prefix = "dbc.auth", name = "enabled", havingValue = "true")
@Component("dbc-APIAuthInterceptor")
@Slf4j
public class APIAuthInterceptor extends AbstractHandlerInterceptor implements ApplicationListener<SMUserEvent>, InitializingBean {

    @Value("${spring.application.name:h-dbc}")
    private String app;

    @Autowired
    private Config conf;

    @Autowired
    private JdbcTemplate jt;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    private volatile Map<String, UserInfo> users = new HashMap<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        String sql = "select username,password from h_users where app=?";
        if (log.isDebugEnabled())
            log.debug("初始化api拦截器加载用户信息, {}, {}", sql, app);
        List<UserInfo> uls = jt.query(sql, new Object[]{app}, new int[]{Types.VARCHAR}, (rs, rowNum) -> {
            UserInfo ui = new UserInfo();
            ui.setUsername(rs.getString(1));
            ui.setPassword(rs.getString(2));
            return ui;
        });
        Map<String, UserInfo> tmp = new HashMap<>(32);
        for (UserInfo ul : uls) {
            tmp.put(ul.getUsername(), ul);
        }
        this.users = tmp;
    }

    @Override
    public void onApplicationEvent(SMUserEvent event) {
        try {
            if (log.isDebugEnabled())
                log.debug("监听到用户信息变化，{}", GsonUtils.toJson(event.getSource()));
            afterPropertiesSet();
        } catch (Exception e) {
            log.error("用户信息变化重新加载用户异常", e);
        }
    }

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
                UserInfo ui = users.get(array[0]);
                if (ui == null) {
                    log.warn("basic认证，账号不存在, {}", array[0]);
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    return false;
                } else {
                    boolean r = encoder.matches(array[1], ui.getPassword());
                    if (log.isDebugEnabled())
                        log.debug("basic认证结果，{}: {}", array[0], r ? "通过" : "不通过");
                    if (r) {
                        com.github.hbq969.code.sm.login.model.UserInfo su = new com.github.hbq969.code.sm.login.model.UserInfo();
                        su.setUserName(ui.getUsername());
                        UserContext.set(su);
                    } else {
                        response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    }
                    return r;
                }
            }
        }
    }

    @Override
    public int order() {
        return 3;
    }
}
