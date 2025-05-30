package com.github.hbq969.middleware.dbc.service.impl.rbac;

import com.github.hbq969.code.common.spring.context.SpringContext;
import com.github.hbq969.code.common.utils.I18nUtils;
import com.github.hbq969.code.sm.login.session.UserContext;
import com.github.hbq969.middleware.dbc.dao.ServiceDao;
import com.github.hbq969.middleware.dbc.dao.entity.ServiceEntity;
import com.github.hbq969.middleware.dbc.service.Service;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;

@org.springframework.stereotype.Service("dbc-ServiceRBACImpl")
public class ServiceRBACImpl implements Service {

    @Autowired
    @Qualifier("dbc-ServiceCacheImpl")
    private Service target;

    @Autowired
    private ServiceDao serviceDao;

    @Autowired
    private SpringContext context;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public synchronized void saveService(ServiceEntity service) {
        target.saveService(service);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public synchronized void updateService(ServiceEntity service) {
        rbac(service);
        target.updateService(service);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public synchronized void deleteService(ServiceEntity service) {
        rbac(service);
        target.deleteService(service);
    }

    @Override
    public PageInfo<ServiceEntity> queryServiceList(ServiceEntity service, int pageNum, int pageSize) {
        return target.queryServiceList(service, pageNum, pageSize);
    }

    @Override
    public ServiceEntity queryService(String id) {
        return target.queryService(id);
    }

    private void rbac(ServiceEntity service) {
        String username = service.getUsername();
        String serviceId = service.getServiceId();
        if (UserContext.get().isAdmin() ||
                UserContext.permitAllow(service.getUsername()) && serviceDao.querySelectCountByUser(serviceId, username) > 0) {
        }else{
            throw new UnsupportedOperationException(I18nUtils.getMessage(context, "BackupServiceImpl.msg1"));
        }
    }
}
