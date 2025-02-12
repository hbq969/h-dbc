package com.github.hbq969.middleware.dbc.service;

import com.github.hbq969.middleware.dbc.dao.entity.ServiceEntity;
import com.github.pagehelper.PageInfo;
import org.springframework.transaction.annotation.Transactional;

public interface Service {
    @Transactional(rollbackFor = Exception.class)
    void saveService(ServiceEntity service);

    void updateService(ServiceEntity service);

    @Transactional(rollbackFor = Exception.class)
    void deleteService(String serviceId);

    PageInfo<ServiceEntity> queryServiceList(ServiceEntity service,int pageNum,int pageSize);
}
