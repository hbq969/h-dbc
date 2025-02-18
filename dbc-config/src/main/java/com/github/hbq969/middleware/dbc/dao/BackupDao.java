package com.github.hbq969.middleware.dbc.dao;

import com.github.hbq969.middleware.dbc.dao.entity.BackupEntity;
import com.github.hbq969.middleware.dbc.dao.entity.ServiceConfigEntity;
import com.github.hbq969.middleware.dbc.model.AccountServiceProfile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("dbc-BackupDao")
@Mapper
public interface BackupDao {
    List<ServiceConfigEntity> queryConfigListByProfileName(@Param("profileName") String profileName);

    List<ServiceConfigEntity> queryConfigListByServiceId(@Param("serviceId") String serviceId);

    List<ServiceConfigEntity> queryConfigListByServiceAndProfile(AccountServiceProfile asp);

    List<BackupEntity> queryBackupList(AccountServiceProfile asp);

    BackupEntity queryBackup(BackupEntity bk);

    void deleteBackup(BackupEntity bk);
}
