package com.github.hbq969.middleware.dbc.service.impl;

import cn.hutool.core.lang.UUID;
import com.github.hbq969.code.common.spring.context.SpringContext;
import com.github.hbq969.code.common.spring.i18n.LangInfo;
import com.github.hbq969.code.common.spring.i18n.LanguageChangeListener;
import com.github.hbq969.code.common.spring.i18n.LanguageEvent;
import com.github.hbq969.code.common.utils.FormatTime;
import com.github.hbq969.code.common.utils.GsonUtils;
import com.github.hbq969.code.common.utils.InitScriptUtils;
import com.github.hbq969.code.dict.service.api.impl.MapDictHelperImpl;
import com.github.hbq969.code.sm.login.event.SMInfoEvent;
import com.github.hbq969.code.sm.login.service.LoginService;
import com.github.hbq969.code.sm.login.session.UserContext;
import com.github.hbq969.middleware.dbc.dao.ServiceDao;
import com.github.hbq969.middleware.dbc.dao.entity.ServiceEntity;
import com.github.hbq969.middleware.dbc.model.AccountService;
import com.github.hbq969.middleware.dbc.service.BackupService;
import com.github.hbq969.middleware.dbc.service.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.event.EventListener;

import java.util.List;
import java.util.Locale;

@org.springframework.stereotype.Service("dbc-ServiceImpl")
@Slf4j
public class ServiceImpl implements Service, InitializingBean, LanguageChangeListener, ApplicationEventPublisherAware {

    @Autowired
    private ServiceDao serviceDao;

    @Autowired
    private SpringContext context;

    @Autowired(required = false)
    private LoginService loginService;

    @Autowired
    private MapDictHelperImpl dict;

    @Autowired
    @Qualifier("dbc-BackupProxyImpl")
    private BackupService backupService;

    private ApplicationEventPublisher eventCenter;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.eventCenter = applicationEventPublisher;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        tableInitial();
        initialScript(null);
    }

    public void initialScript(LangInfo langInfo) {
        if (langInfo == null) {
            log.info("h-dbc启动时，初始化脚本数据.");
        } else {
            log.info("h-dbc监听到语言变化通知: {}，初始化脚本数据.", GsonUtils.toJson(langInfo));
        }
        if (loginService != null) {
            String language = String.join("-", Locale.getDefault().getLanguage(), Locale.getDefault().getCountry());
            String scriptFile;
            if (StringUtils.equals("zh-CN", language)) {
                scriptFile = "h-dbc-data.sql";
            } else {
                scriptFile = "h-dbc-data-" + language + ".sql";
            }
            loginService.finished(() -> InitScriptUtils.initial(context, scriptFile, () -> {
                dict.reloadImmediately();
                loginService.loadSMInfo(new SMInfoEvent("h-dbc"));
            }));
        }
    }

    @Override
    public String listenerName() {
        return "h-dbc/service";
    }

    @Override
    public int listenerOrder() {
        return 1;
    }

    @Override
    public void onChange(LangInfo langInfo) {
        initialScript(langInfo);
    }

    @Override
    public void saveService(ServiceEntity service) {
        AccountService accountService = new AccountService();
        accountService.userInitial(context);

        List<ServiceEntity> ses = serviceDao.queryServiceByName(accountService, service.getServiceName());
        if (CollectionUtils.isNotEmpty(ses)) {
            throw new IllegalArgumentException(String.format("该服务名已被 [%s] 创建", ses.get(0).getUsername()));
        }

        service.setServiceId(UUID.fastUUID().toString(true));
        service.setCreatedAt(FormatTime.nowSecs());
        serviceDao.saveService(service);

        accountService.setServiceId(service.getServiceId());
        serviceDao.saveAccountService(accountService);
    }

    @Override
    public void updateService(ServiceEntity service) {

        if (UserContext.permitAllow(service.getUsername())) {
            service.setUpdatedAt(FormatTime.nowSecs());
            serviceDao.updateService(service);
        } else {
            throw new UnsupportedOperationException("当前账号无此操作权限");
        }
    }

    @Override
    public void deleteService(ServiceEntity service) {

        if (UserContext.permitAllow(service.getUsername())) {
            backupService.backupOnDeleteService(service);
            serviceDao.deleteServiceOnAdmin(service);
            serviceDao.deleteAccServiceOnAdmin(service.getServiceId());
            serviceDao.deleteServiceConfigOnAdmin(service.getServiceId());
            serviceDao.deleteServiceConfigFileOnAdmin(service.getServiceId());
        } else {
            throw new UnsupportedOperationException("账号无此操作权限");
        }
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

    private void tableInitial() {
        try {
            serviceDao.createService();
            log.info("表h_dbc_service创建成功");
        } catch (Exception e) {
            log.error("表h_dbc_service已存在");
        }
        try {
            serviceDao.createAccService();
            log.info("表h_dbc_acc_service创建成功");
        } catch (Exception e) {
            log.error("表h_dbc_acc_service已存在");
        }
        try {
            serviceDao.createProfiles();
            log.info("表h_dbc_profiles创建成功");
        } catch (Exception e) {
            log.error("表h_dbc_profiles已存在");
        }
        try {
            serviceDao.createAccProfiles();
            log.info("表h_dbc_acc_profiles创建成功");
        } catch (Exception e) {
            log.error("表h_dbc_acc_profiles已存在");
        }
        try {
            serviceDao.createConfig();
            log.info("表h_dbc_config创建成功");
        } catch (Exception e) {
            log.error("表h_dbc_config已存在");
        }
        try {
            serviceDao.createConfigFile();
            log.info("表h_dbc_config_file创建成功");
        } catch (Exception e) {
            log.error("表h_dbc_config_file已存在");
        }
        try {
            serviceDao.createConfigBackup();
            log.info("表h_dbc_config_bk创建成功");
        } catch (Exception e) {
            log.error("表h_dbc_config_bk已存在");
        }
    }
}
