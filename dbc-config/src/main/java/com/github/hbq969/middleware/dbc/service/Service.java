package com.github.hbq969.middleware.dbc.service;

import com.github.hbq969.middleware.dbc.dao.entity.ServiceEntity;
import com.github.pagehelper.PageInfo;

public interface Service {
    void saveService(ServiceEntity service);

    void updateService(ServiceEntity service);

    void deleteService(ServiceEntity service);

    PageInfo<ServiceEntity> queryServiceList(ServiceEntity service,int pageNum,int pageSize);
}
