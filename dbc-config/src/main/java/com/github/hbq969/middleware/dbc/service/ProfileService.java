package com.github.hbq969.middleware.dbc.service;

import com.github.hbq969.middleware.dbc.dao.entity.ProfileEntity;
import com.github.hbq969.middleware.dbc.model.AccountService;
import com.github.hbq969.middleware.dbc.model.AccountServiceProfile;
import com.github.pagehelper.PageInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ProfileService {

    @Transactional(rollbackFor = Exception.class)
    void saveProfile(ProfileEntity profile);

    void updateProfile(ProfileEntity profile);

    @Transactional(rollbackFor = Exception.class)
    void deleteProfile(ProfileEntity profile);

    PageInfo<ProfileEntity> queryProfileList(ProfileEntity profile, int pageNum, int pageSize);

    List<ProfileEntity> queryProfileList(AccountService as);

    void deleteProfileConfig(AccountServiceProfile asp);
}
