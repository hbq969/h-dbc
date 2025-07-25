package com.github.hbq969.middleware.dbc.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.UUID;
import com.github.hbq969.code.common.initial.AbstractScriptInitialAware;
import com.github.hbq969.code.common.initial.ScriptInitialAware;
import com.github.hbq969.code.common.spring.context.SpringContext;
import com.github.hbq969.code.common.spring.i18n.LangInfo;
import com.github.hbq969.code.common.spring.i18n.LanguageEvent;
import com.github.hbq969.code.common.utils.FormatTime;
import com.github.hbq969.code.common.utils.GsonUtils;
import com.github.hbq969.code.common.utils.I18nUtils;
import com.github.hbq969.code.common.utils.StrUtils;
import com.github.hbq969.code.dict.service.api.impl.MapDictHelperImpl;
import com.github.hbq969.code.sm.login.service.LoginService;
import com.github.hbq969.middleware.dbc.dao.ProfileDao;
import com.github.hbq969.middleware.dbc.dao.ServiceDao;
import com.github.hbq969.middleware.dbc.dao.entity.ServiceEntity;
import com.github.hbq969.middleware.dbc.model.AccountService;
import com.github.hbq969.middleware.dbc.service.BackupService;
import com.github.hbq969.middleware.dbc.service.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@org.springframework.stereotype.Service("dbc-ServiceImpl")
@Slf4j
public class ServiceImpl extends AbstractScriptInitialAware implements Service {

    @Autowired
    private ServiceDao serviceDao;

    @Autowired
    private SpringContext context;

    @Autowired
    private LoginService loginService;

    @Autowired
    @Qualifier("dbc-BackupServiceRBACImpl")
    private BackupService backupService;

    @Qualifier("dbc-ProfileDao")
    @Autowired
    private ProfileDao profileDao;

    @Override
    public void saveService(ServiceEntity service) {
        AccountService accountService = new AccountService();
        accountService.userInitial(context);

        List<ServiceEntity> ses = serviceDao.queryServiceByName(accountService, service.getServiceName());
        if (CollectionUtils.isNotEmpty(ses)) {
            throw new IllegalArgumentException(String.format(I18nUtils.getMessage(context, "ServiceImpl.saveService.msg1"), ses.get(0).getUsername()));
        }

        service.setServiceId(UUID.fastUUID().toString(true));
        service.setCreatedAt(FormatTime.nowSecs());
        serviceDao.saveService(service);

        accountService.setServiceId(service.getServiceId());
        serviceDao.saveAccountService(accountService);
    }

    @Override
    public void updateService(ServiceEntity service) {
        serviceDao.updateService(service);
    }

    @Override
    public void deleteService(ServiceEntity service) {
        backupService.backupOnDeleteService(service);
        serviceDao.deleteServiceOnAdmin(service);
        serviceDao.deleteAccServiceOnAdmin(service.getServiceId());
        serviceDao.deleteServiceConfigOnAdmin(service.getServiceId());
        serviceDao.deleteServiceConfigFileOnAdmin(service.getServiceId());
    }

    @Override
    public PageInfo<ServiceEntity> queryServiceList(ServiceEntity service, int pageNum, int pageSize) {
        AccountService accountService = new AccountService();
        accountService.userInitial(context);
        accountService.setServiceId(service.getServiceId());
        PageInfo<ServiceEntity> pg = PageHelper.startPage(pageNum, pageSize)
                .doSelectPageInfo(() -> serviceDao.queryServiceList(accountService, service));
        pg.getList().forEach(se -> se.convertDict(context));
        return pg;
    }

    @Override
    public ServiceEntity queryService(String id) {
        return serviceDao.queryService(id);
    }

    @Override
    public String nameOfScriptInitialAware() {
        return "h-dbc-script-initial";
    }

    @Override
    public void onApplicationEvent(LanguageEvent event) {
        LangInfo langInfo = (LangInfo) event.getSource();
        if (log.isDebugEnabled()) {
            log.debug("h-dbc 监听到语言变化事件: {}", GsonUtils.toJson(langInfo));
        }
        scriptInitial();
    }

    @Override
    public void scriptInitial0() {
        String lang = com.github.hbq969.code.sm.login.utils.I18nUtils.getFullLanguage(context);
        String filename = String.join("", "dbc-initial", "-", lang, ".sql");
        Map map = ImmutableMap.of("defaultDataBaseSchema", getDefaultDatabaseName(context.getBean(JdbcTemplate.class)),
                "menuPrefixPath", context.getProperty("dbc.menu.prefix-path", ""));
        com.github.hbq969.code.common.utils.InitScriptUtils.initial(context, filename, StandardCharsets.UTF_8,
                (sql) -> StrUtils.replacePlaceHolders(sql, map, "dbc"),
                () -> {
                    loginService.loadSMInfo();
                    context.getOptionalBean(MapDictHelperImpl.class).ifPresent(dict -> dict.reloadImmediately());
                });
    }

    @Override
    public int orderOfScriptInitialAware() {
        return 20;
    }

    @Override
    public void tableCreate0() {
        log.debug("h-dbc 初始化创建所有的表。");
        try {
            serviceDao.createService();
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("表h_dbc_service已存在");
            }
        }
        try {
            serviceDao.createAccService();
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("表h_dbc_acc_service已存在");
            }
        }
        try {
            serviceDao.createProfiles();
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("表h_dbc_profiles已存在");
            }
        }
        try {
            serviceDao.createAccProfiles();
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("表h_dbc_acc_profiles已存在");
            }
        }
        try {
            serviceDao.createConfig();
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("表h_dbc_config已存在");
            }
        }
        try {
            serviceDao.createConfigFile();
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("表h_dbc_config_file已存在");
            }
        }
        try {
            serviceDao.createConfigBackup();
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("表h_dbc_config_bk已存在");
            }
        }
    }

    private String getDefaultDatabaseName(JdbcTemplate jt) {
        Assert.notNull(jt, "缺少默认的jdbc数据源");
        Connection c = null;
        try {
            c = jt.getDataSource().getConnection();
            DatabaseMetaData metaData = c.getMetaData();
            String dcn = metaData.getDriverName().toLowerCase();
            if (dcn.contains("mysql")) {
                return extractDatabaseName(metaData.getURL());
            } else if (dcn.contains("oracle")) {
                return metaData.getUserName();
            } else if (dcn.contains("h2")) {
                return "dbc";
            } else {
                throw new UnsupportedOperationException(String.format("不支持的缺省数据源: %s", dcn));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (c != null) {
                try {
                    c.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static String extractDatabaseName(String jdbcUrl) {
        try {
            // 去掉 jdbc:mysql:// 前缀
            String urlWithoutPrefix = jdbcUrl.substring("jdbc:mysql://".length());
            // 解析 URL
            URL url = new URL("http://" + urlWithoutPrefix);
            // 获取路径部分
            String path = url.getPath();
            // 去掉路径前的斜杠
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            // 返回路径部分作为数据库名称
            return path;
        } catch (Exception e) {
            log.error("", e);
            return null;
        }
    }
}
