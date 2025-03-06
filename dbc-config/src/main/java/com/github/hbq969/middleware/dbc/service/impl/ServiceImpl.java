package com.github.hbq969.middleware.dbc.service.impl;

import cn.hutool.core.lang.UUID;
import com.github.hbq969.code.common.spring.context.SpringContext;
import com.github.hbq969.code.common.utils.FormatTime;
import com.github.hbq969.code.common.utils.I18nUtils;
import com.github.hbq969.code.dict.service.api.impl.MapDictHelperImpl;
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
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

import java.util.List;

@org.springframework.stereotype.Service("dbc-ServiceImpl")
@Slf4j
public class ServiceImpl implements Service, InitializingBean, ApplicationEventPublisherAware {

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
    }

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

        if (UserContext.permitAllow(service.getUsername())) {
            service.setUpdatedAt(FormatTime.nowSecs());
            serviceDao.updateService(service);
        } else {
            throw new UnsupportedOperationException(I18nUtils.getMessage(context, "BackupServiceImpl.msg1"));
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
            throw new UnsupportedOperationException(I18nUtils.getMessage(context, "BackupServiceImpl.msg1"));
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
}
