package com.github.hbq969.middleware.dbc.service.impl;

import cn.hutool.core.lang.UUID;
import com.github.hbq969.code.common.spring.context.SpringContext;
import com.github.hbq969.code.common.utils.FormatTime;
import com.github.hbq969.code.common.utils.InitScriptUtils;
import com.github.hbq969.code.dict.service.api.impl.MapDictHelperImpl;
import com.github.hbq969.code.sm.login.service.LoginService;
import com.github.hbq969.code.sm.login.session.UserContext;
import com.github.hbq969.middleware.dbc.dao.ServiceDao;
import com.github.hbq969.middleware.dbc.dao.entity.ServiceEntity;
import com.github.hbq969.middleware.dbc.model.AccountService;
import com.github.hbq969.middleware.dbc.service.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@org.springframework.stereotype.Service("dbc-ServiceImpl")
@Slf4j
public class ServiceImpl implements Service, InitializingBean {

    @Autowired
    private ServiceDao serviceDao;

    @Autowired
    private SpringContext context;

    @Autowired(required = false)
    private LoginService loginService;

    @Autowired
    private MapDictHelperImpl dict;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (loginService != null) {
            loginService.finished(() -> InitScriptUtils.initial(context, "h-dbc-data.sql", () -> dict.reloadImmediately()));
        }
        tableInitial();
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
        service.setUpdatedAt(FormatTime.nowSecs());
        serviceDao.updateService(service);
    }

    @Override
    public void deleteService(String serviceId) {
        ServiceEntity service = new ServiceEntity();
        service.setServiceId(serviceId);
        if (UserContext.get().isAdmin()) {
            serviceDao.deleteServiceOnAdmin(service);
            serviceDao.deleteAccServiceOnAdmin(serviceId);
            serviceDao.deleteServiceConfigOnAdmin(serviceId);
        } else {
            AccountService as = new AccountService();
            as.userInitial(context);
            as.setServiceId(serviceId);
            serviceDao.deleteService(as);
            serviceDao.deleteAccService(as);
            serviceDao.deleteServiceConfig(as);
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
    }
}
