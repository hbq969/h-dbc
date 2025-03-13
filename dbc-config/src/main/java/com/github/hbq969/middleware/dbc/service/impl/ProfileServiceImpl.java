package com.github.hbq969.middleware.dbc.service.impl;

import com.github.hbq969.code.common.spring.context.SpringContext;
import com.github.hbq969.code.common.utils.FormatTime;
import com.github.hbq969.code.common.utils.I18nUtils;
import com.github.hbq969.code.sm.login.session.UserContext;
import com.github.hbq969.middleware.dbc.dao.ProfileDao;
import com.github.hbq969.middleware.dbc.dao.ServiceDao;
import com.github.hbq969.middleware.dbc.dao.entity.ProfileEntity;
import com.github.hbq969.middleware.dbc.model.AccountProfile;
import com.github.hbq969.middleware.dbc.model.AccountService;
import com.github.hbq969.middleware.dbc.model.AccountServiceProfile;
import com.github.hbq969.middleware.dbc.service.BackupService;
import com.github.hbq969.middleware.dbc.service.ProfileService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service("dbc-ProfileServiceImpl")
public class ProfileServiceImpl implements ProfileService {
    @Autowired
    private ProfileDao profileDao;
    @Autowired
    private SpringContext context;
    @Autowired
    @Qualifier("dbc-BackupServiceRBACImpl")
    private BackupService backupService;
    @Qualifier("dbc-ServiceDao")
    @Autowired
    private ServiceDao serviceDao;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveProfile(ProfileEntity profile) {
        List<ProfileEntity> pes = profileDao.queryProfileByName(profile.getProfileName());
        if (CollectionUtils.isNotEmpty(pes)) {
            throw new IllegalArgumentException(String.format(I18nUtils.getMessage(context, "ProfileServiceImpl.saveProfile.msg2"), pes.get(0).getUsername()));
        }
        profile.setCreatedAt(FormatTime.nowSecs());
        profileDao.saveProfile(profile);
        AccountProfile accountProfile = new AccountProfile();
        accountProfile.userInitial(context);
        accountProfile.setProfileName(profile.getProfileName());
        profileDao.saveAccProfile(accountProfile);
    }

    @Override
    public void updateProfile(ProfileEntity profile) {
        profileDao.updateProfile(profile);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteProfile(ProfileEntity profile) {
        backupService.backupOnDeleteProfile(profile);
        profileDao.deleteProfileOnAdmin(profile.getProfileName());
        profileDao.deleteAccProfileOnAdmin(profile.getProfileName());
        profileDao.deleteProfileAllConfigOnAdmin(profile.getProfileName());
        profileDao.deleteProfileConfigFileOnAdmin(profile.getProfileName());
    }

    @Override
    public PageInfo<ProfileEntity> queryProfileList(ProfileEntity profile, int pageNum, int pageSize) {
        PageInfo<ProfileEntity> pg = PageHelper.startPage(pageNum, pageSize)
                .doSelectPageInfo(() -> profileDao.queryProfileList(profile));
        pg.getList().forEach(p -> p.convertDict(context));
        return pg;
    }

    @Override
    public List<ProfileEntity> queryProfileList(AccountService as) {
        String currentUserName = UserContext.get().getUserName();
        String serviceId = as.getServiceId();
        if (UserContext.get().isAdmin() ||
                UserContext.permitAllow(as.getUsername()) && serviceDao.querySelectCountByUser(serviceId, currentUserName) > 0) {
            List<ProfileEntity> all = profileDao.queryAllProfileList();
            as.setApp(context.getProperty("spring.application.name"));
            List<ProfileEntity> config = profileDao.queryProfileConfigNum(as);
            Map<String, Integer> map = config.stream().collect(Collectors.toMap(p -> p.getProfileName(), p -> p.getConfigNum(), (p1, p2) -> p2));
            for (ProfileEntity profile : all) {
                int configNum = MapUtils.getIntValue(map, profile.getProfileName(), 0);
                profile.setConfigNum(configNum);
            }
            return all;
        } else {
            throw new UnsupportedOperationException(I18nUtils.getMessage(context, "BackupServiceImpl.msg1"));
        }
    }

    @Override
    public void deleteProfileConfig(AccountServiceProfile asp) {
        asp.setApp(context.getProperty("spring.application.name"));
        backupService.backupOnClearProfileConfig(asp);
        profileDao.deleteProfileConfig(asp);
        profileDao.deleteProfileConfileFile2(asp);
    }

    @Override
    public void backup(ProfileEntity profile) {
        backupService.backupOnDeleteProfile(profile);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void backupAll() {
        List<ProfileEntity> profiles = profileDao.queryAllProfileList();
        for (ProfileEntity profile : profiles) {
            backupService.backupOnDeleteProfile(profile);
        }
    }
}
