package com.github.hbq969.middleware.dbc.service.impl;

import com.github.hbq969.middleware.dbc.config.Config;
import com.github.hbq969.middleware.dbc.dao.entity.*;
import com.github.hbq969.middleware.dbc.model.AccountServiceProfile;
import com.github.hbq969.middleware.dbc.service.BackupService;
import com.github.hbq969.middleware.dbc.view.request.BatchDeleteBackup;
import com.github.hbq969.middleware.dbc.view.request.BatchDeleteRecovery;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service("dbc-BackupProxyImpl")
public class BackupProxyImpl implements BackupService {

    @Autowired
    @Qualifier("dbc-BackupServiceImpl")
    private BackupService target;

    @Autowired
    private Config conf;

    @Override
    public void backupOnDeleteProfile(ProfileEntity profile) {
        if (conf.isBackup()) {
            target.backupOnDeleteProfile(profile);
        }
    }

    @Override
    public void backupOnDeleteService(ServiceEntity service) {
        if (conf.isBackup()) {
            target.backupOnDeleteService(service);
        }
    }

    @Override
    public void backupOnClearProfileConfig(AccountServiceProfile asp) {
        if (conf.isBackup()) {
            target.backupOnClearProfileConfig(asp);
        }
    }

    @Override
    public void backupOnConfigImport(AccountServiceProfile asp) {
        if (conf.isBackup()) {
            target.backupOnConfigImport(asp);
        }
    }

    @Override
    public void backupOnUpdateConfigFile(ConfigFileEntity file) {
        if (conf.isBackup()) {
            target.backupOnUpdateConfigFile(file);
        }
    }

    @Override
    public PageInfo<BackupEntity> queryBackupList(AccountServiceProfile asp, int pageNum, int pageSize) {
        if (conf.isBackup()) {
            return target.queryBackupList(asp, pageNum, pageSize);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteBackup(BackupEntity bk) {
        if(conf.isBackup()){
            target.deleteBackup(bk);
        }
    }

    @Override
    public void deleteBackups(BatchDeleteBackup bdb) {
        if(conf.isBackup()){
            target.deleteBackups(bdb);
        }
    }

    @Override
    public void recoveryBackup(BackupEntity bk) {
        if(conf.isBackup()){
            target.recoveryBackup(bk);
        }
    }

    @Override
    public void recoveryBackups(BatchDeleteRecovery bdr) {
        if(conf.isBackup()){
            target.recoveryBackups(bdr);
        }
    }

    @Override
    public List<ProfileEntity> queryProfileList() {
        if(conf.isBackup()){
            return target.queryProfileList();
        }
        throw new UnsupportedOperationException();
    }
}
