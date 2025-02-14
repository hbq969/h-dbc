package com.github.hbq969.middleware.dbc.dao;

import com.github.hbq969.middleware.dbc.dao.entity.ProfileEntity;
import com.github.hbq969.middleware.dbc.model.AccountProfile;
import com.github.hbq969.middleware.dbc.model.AccountService;
import com.github.hbq969.middleware.dbc.model.AccountServiceProfile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProfileDao {
    void saveProfile(ProfileEntity profile);

    void saveAccProfile(AccountProfile accountProfile);

    void updateProfile(ProfileEntity profile);

    void deleteProfileOnAdmin(@Param("name") String name);

    void deleteAccProfileOnAdmin(@Param("name") String name);

    void deleteProfileAllConfigOnAdmin(@Param("name") String name);

    void deleteProfileConfigFileOnAdmin(@Param("name") String name);

    void deleteProfile(AccountProfile ap);

    void deleteAccProfile(AccountProfile ap);

    void deleteProfileAllConfig(AccountProfile ap);

    void deleteProfileConfigFile(AccountProfile ap);

    List<ProfileEntity> queryProfileList(ProfileEntity profile);

    List<ProfileEntity> queryAllProfileList();

    List<ProfileEntity> queryProfileConfigNum(AccountService as);

    List<ProfileEntity> queryProfileByName(@Param("name") String name);

    void deleteProfileConfig(AccountServiceProfile asp);

    void deleteProfileConfileFile2(AccountServiceProfile asp);
}
