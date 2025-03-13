package com.github.hbq969.middleware.dbc.service.impl;

import com.github.hbq969.code.common.spring.context.SpringContext;
import com.github.hbq969.code.common.utils.I18nUtils;
import com.github.hbq969.code.sm.login.session.UserContext;
import com.github.hbq969.middleware.dbc.dao.ServiceDao;
import com.github.hbq969.middleware.dbc.dao.entity.ProfileEntity;
import com.github.hbq969.middleware.dbc.model.AccountService;
import com.github.hbq969.middleware.dbc.model.AccountServiceProfile;
import com.github.hbq969.middleware.dbc.service.ProfileService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("dbc-ProfileServiceRBACImpl")
public class ProfileServiceRBACImpl implements ProfileService {

    @Autowired
    @Qualifier("dbc-ProfileServiceImpl")
    private ProfileService target;

    @Autowired
    private SpringContext context;

    @Autowired
    private ServiceDao serviceDao;

    @Override
    public void saveProfile(ProfileEntity profile) {
        rbac();
        target.saveProfile(profile);
    }

    @Override
    public void updateProfile(ProfileEntity profile) {
        rbac();
        target.updateProfile(profile);
    }

    @Override
    public void deleteProfile(ProfileEntity profile) {
        rbac();
        target.deleteProfile(profile);
    }

    @Override
    public PageInfo<ProfileEntity> queryProfileList(ProfileEntity profile, int pageNum, int pageSize) {
        return target.queryProfileList(profile, pageNum, pageSize);
    }

    @Override
    public List<ProfileEntity> queryProfileList(AccountService as) {
        return target.queryProfileList(as);
    }

    @Override
    public void deleteProfileConfig(AccountServiceProfile asp) {
        String serviceId = asp.getServiceId();
        String currentUserName = UserContext.get().getUserName();
        if (UserContext.get().isAdmin() ||
                UserContext.permitAllow(asp.getUsername()) && serviceDao.querySelectCountByUser(serviceId, currentUserName) > 0) {
        } else {
            throw new UnsupportedOperationException(I18nUtils.getMessage(context, "BackupServiceImpl.msg1"));
        }
        target.deleteProfileConfig(asp);
    }

    @Override
    public void backup(ProfileEntity profile) {
        rbac();
        target.backup(profile);
    }

    @Override
    public void backupAll() {
        rbac();
        target.backupAll();
    }

    private void rbac() {
        if (!UserContext.get().isAdmin()) {
            throw new UnsupportedOperationException(I18nUtils.getMessage(context, "ProfileServiceImpl.saveProfile.msg1"));
        }
    }
}
