package com.github.hbq969.middleware.dbc.service;

import com.github.hbq969.middleware.dbc.dao.entity.BackupEntity;
import com.github.hbq969.middleware.dbc.dao.entity.ConfigFileEntity;
import com.github.hbq969.middleware.dbc.dao.entity.ProfileEntity;
import com.github.hbq969.middleware.dbc.dao.entity.ServiceEntity;
import com.github.hbq969.middleware.dbc.model.AccountServiceProfile;
import com.github.hbq969.middleware.dbc.view.request.BatchDeleteBackup;
import com.github.hbq969.middleware.dbc.view.request.BatchDeleteRecovery;
import com.github.pagehelper.PageInfo;

import java.util.List;

public interface BackupService {
    void backupOnDeleteProfile(ProfileEntity profile);

    void backupOnDeleteService(ServiceEntity service);

    void backupOnClearProfileConfig(AccountServiceProfile asp);

    void backupOnConfigImport(AccountServiceProfile asp);

    void backupOnUpdateConfigFile(ConfigFileEntity file);

    PageInfo<BackupEntity> queryBackupList(AccountServiceProfile asp, int pageNum, int pageSize);

    void deleteBackup(BackupEntity bk);

    void deleteBackups(BatchDeleteBackup bdb);

    void recoveryBackup(BackupEntity bk);

    void recoveryBackups(BatchDeleteRecovery bdr);

    List<ProfileEntity> queryProfileList();
}
