package com.github.hbq969.middleware.dbc.service.impl.cache;

import com.github.hbq969.middleware.dbc.dao.ProfileDao;
import com.github.hbq969.middleware.dbc.dao.ServiceDao;
import com.github.hbq969.middleware.dbc.dao.entity.ProfileEntity;
import com.github.hbq969.middleware.dbc.dao.entity.ServiceEntity;
import com.github.hbq969.middleware.dbc.model.APIModel;
import com.github.hbq969.middleware.dbc.service.CacheService;
import com.github.hbq969.middleware.dbc.service.Service;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@org.springframework.stereotype.Service("dbc-ServiceCacheImpl")
public class ServiceCacheImpl implements Service {

    @Autowired
    @Qualifier("dbc-ServiceImpl")
    private Service target;

    @Autowired
    private ServiceDao serviceDao;

    @Autowired
    private ProfileDao profileDao;

    @Autowired
    private CacheService cacheService;

    @Override
    public void saveService(ServiceEntity service) {
        target.saveService(service);
    }

    @Override
    public void updateService(ServiceEntity service) {
        target.updateService(service);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteService(ServiceEntity service) {
        try {
            ServiceEntity old = serviceDao.queryService(service.getServiceId());
            List<ProfileEntity> profiles = profileDao.queryAllProfileList();
            for (ProfileEntity profile : profiles) {
                APIModel model = new APIModel();
                model.setProfileName(profile.getProfileName());
                model.setServiceName(old.getServiceName());
                cacheService.clear(model);
            }
        } finally {
            target.deleteService(service);
        }
    }

    @Override
    public PageInfo<ServiceEntity> queryServiceList(ServiceEntity service, int pageNum, int pageSize) {
        return target.queryServiceList(service, pageNum, pageSize);
    }

    @Override
    public ServiceEntity queryService(String id) {
        return target.queryService(id);
    }
}
