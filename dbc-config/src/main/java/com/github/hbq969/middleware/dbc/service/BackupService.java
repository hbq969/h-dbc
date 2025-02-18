package com.github.hbq969.middleware.dbc.service;

import com.github.hbq969.middleware.dbc.dao.entity.*;
import com.github.hbq969.middleware.dbc.model.AccountServiceProfile;
import com.github.pagehelper.PageInfo;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface BackupService {
    void backupOnDeleteProfile(ProfileEntity profile);

    void backupOnDeleteService(ServiceEntity service);

    void backupOnClearProfileConfig(AccountServiceProfile asp);

    void backupOnConfigImport(AccountServiceProfile asp);

    void backupOnUpdateConfigFile(ConfigFileEntity file);

    PageInfo<BackupEntity> queryBackupList(AccountServiceProfile asp, int pageNum, int pageSize);

    void deleteBackup(BackupEntity bk);

    @Transactional(rollbackFor = Exception.class)
    void deleteBackups(List<BackupEntity> list);

    @Transactional(rollbackFor = Exception.class)
    void recoveryBackup(BackupEntity bk);

    @Transactional(rollbackFor = Exception.class)
    void recoveryBackups(List<BackupEntity> bks);

    List<ProfileEntity> queryProfileList();
}
