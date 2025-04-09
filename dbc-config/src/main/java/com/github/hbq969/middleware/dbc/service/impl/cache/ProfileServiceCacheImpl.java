package com.github.hbq969.middleware.dbc.service.impl.cache;

import com.github.hbq969.code.common.spring.context.SpringContext;
import com.github.hbq969.middleware.dbc.dao.ServiceDao;
import com.github.hbq969.middleware.dbc.dao.entity.ProfileEntity;
import com.github.hbq969.middleware.dbc.dao.entity.ServiceEntity;
import com.github.hbq969.middleware.dbc.model.APIModel;
import com.github.hbq969.middleware.dbc.model.AccountService;
import com.github.hbq969.middleware.dbc.model.AccountServiceProfile;
import com.github.hbq969.middleware.dbc.service.CacheService;
import com.github.hbq969.middleware.dbc.service.ProfileService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("dbc-ProfileServiceCacheImpl")
public class ProfileServiceCacheImpl implements ProfileService {

    @Autowired
    @Qualifier("dbc-ProfileServiceImpl")
    private ProfileService target;

    @Autowired
    private ServiceDao serviceDao;

    @Autowired
    private CacheService cacheService;

    @Override
    public void saveProfile(ProfileEntity profile) {
        this.target.saveProfile(profile);
    }

    @Override
    public void updateProfile(ProfileEntity profile) {
        this.target.updateProfile(profile);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteProfile(ProfileEntity profile) {
        try {
            List<ServiceEntity> services = serviceDao.queryAllServices();
            for (ServiceEntity service : services) {
                APIModel model = new APIModel();
                model.setServiceName(service.getServiceName());
                model.setProfileName(profile.getProfileName());
                cacheService.clear(model);
            }
        } finally {
            this.target.deleteProfile(profile);
        }
    }

    @Override
    public PageInfo<ProfileEntity> queryProfileList(ProfileEntity profile, int pageNum, int pageSize) {
        return this.target.queryProfileList(profile, pageNum, pageSize);
    }

    @Override
    public List<ProfileEntity> queryProfileList(AccountService as) {
        return this.target.queryProfileList(as);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteProfileConfig(AccountServiceProfile asp) {
        try {
            ServiceEntity service = serviceDao.queryService(asp.getServiceId());
            asp.setServiceName(service.getServiceName());
            cacheService.clear(new APIModel().of(asp));
        } finally {
            this.target.deleteProfileConfig(asp);
        }
    }

    @Override
    public void backup(ProfileEntity profile) {
        this.target.backup(profile);
    }

    @Override
    public void backupAll() {
        this.target.backupAll();
    }
}
