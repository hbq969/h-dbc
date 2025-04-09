package com.github.hbq969.middleware.dbc.service.impl.cache;

import com.github.hbq969.middleware.dbc.dao.entity.BackupEntity;
import com.github.hbq969.middleware.dbc.dao.entity.ConfigFileEntity;
import com.github.hbq969.middleware.dbc.dao.entity.ProfileEntity;
import com.github.hbq969.middleware.dbc.dao.entity.ServiceEntity;
import com.github.hbq969.middleware.dbc.model.APIModel;
import com.github.hbq969.middleware.dbc.model.AccountServiceProfile;
import com.github.hbq969.middleware.dbc.service.BackupService;
import com.github.hbq969.middleware.dbc.service.CacheService;
import com.github.hbq969.middleware.dbc.view.request.BatchDeleteBackup;
import com.github.hbq969.middleware.dbc.view.request.BatchDeleteRecovery;
import com.github.pagehelper.PageInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("dbc-BackupServiceCacheImpl")
public class BackupServiceCacheImpl implements BackupService {

    @Autowired
    @Qualifier("dbc-BackupServiceImpl")
    private BackupService target;

    @Autowired
    private CacheService cacheService;

    @Override
    public void backupOnDeleteProfile(ProfileEntity profile) {
        this.target.backupOnDeleteProfile(profile);
    }

    @Override
    public void backupOnDeleteService(ServiceEntity service) {
        this.target.backupOnDeleteService(service);
    }

    @Override
    public void backupOnClearProfileConfig(AccountServiceProfile asp) {
        this.target.backupOnClearProfileConfig(asp);
    }

    @Override
    public void backupOnConfigImport(AccountServiceProfile asp) {
        this.target.backupOnConfigImport(asp);
    }

    @Override
    public void backupOnUpdateConfigFile(ConfigFileEntity file) {
        this.target.backupOnUpdateConfigFile(file);
    }

    @Override
    public PageInfo<BackupEntity> queryBackupList(AccountServiceProfile asp, int pageNum, int pageSize) {
        return this.target.queryBackupList(asp, pageNum, pageSize);
    }

    @Override
    public void deleteBackup(BackupEntity bk) {
        this.target.deleteBackup(bk);
    }

    @Override
    public void deleteBackups(BatchDeleteBackup bdb) {
        this.target.deleteBackups(bdb);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void recoveryBackup(BackupEntity bk) {
        try {
            cacheService.clear(new APIModel().of(bk));
        } finally {
            this.target.recoveryBackup(bk);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void recoveryBackups(BatchDeleteRecovery bdr) {
        try {
            if (bdr != null && CollectionUtils.isNotEmpty(bdr.getRecoveries()))
                for (BackupEntity be : bdr.getRecoveries()) {
                    cacheService.clear(new APIModel().of(be));
                }
        } finally {
            this.target.recoveryBackups(bdr);
        }
    }

    @Override
    public List<ProfileEntity> queryProfileList() {
        return this.target.queryProfileList();
    }
}
