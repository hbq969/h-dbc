package com.github.hbq969.middleware.dbc.service.impl.rbac;

import com.github.hbq969.code.common.spring.context.SpringContext;
import com.github.hbq969.code.common.utils.I18nUtils;
import com.github.hbq969.code.sm.login.session.UserContext;
import com.github.hbq969.middleware.dbc.dao.ServiceDao;
import com.github.hbq969.middleware.dbc.dao.entity.BackupEntity;
import com.github.hbq969.middleware.dbc.dao.entity.ConfigFileEntity;
import com.github.hbq969.middleware.dbc.dao.entity.ProfileEntity;
import com.github.hbq969.middleware.dbc.dao.entity.ServiceEntity;
import com.github.hbq969.middleware.dbc.model.AccountServiceProfile;
import com.github.hbq969.middleware.dbc.service.BackupService;
import com.github.hbq969.middleware.dbc.view.request.BatchDeleteBackup;
import com.github.hbq969.middleware.dbc.view.request.BatchDeleteRecovery;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("dbc-BackupServiceRBACImpl")
public class BackupServiceRBACImpl implements BackupService {

    @Autowired
    private SpringContext context;

    @Autowired
    private ServiceDao serviceDao;

    @Autowired
    @Qualifier("dbc-BackupProxyImpl")
    private BackupService target;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public synchronized void backupOnDeleteProfile(ProfileEntity profile) {
        target.backupOnDeleteProfile(profile);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public synchronized void backupOnDeleteService(ServiceEntity service) {
        target.backupOnDeleteService(service);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public synchronized void backupOnClearProfileConfig(AccountServiceProfile asp) {
        target.backupOnClearProfileConfig(asp);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public synchronized void backupOnConfigImport(AccountServiceProfile asp) {
        target.backupOnConfigImport(asp);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public synchronized void backupOnUpdateConfigFile(ConfigFileEntity file) {
        target.backupOnUpdateConfigFile(file);
    }

    @Override
    public PageInfo<BackupEntity> queryBackupList(AccountServiceProfile asp, int pageNum, int pageSize) {
        return target.queryBackupList(asp, pageNum, pageSize);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public synchronized void deleteBackup(BackupEntity bk) {
        rbac(bk);
        target.deleteBackup(bk);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public synchronized void deleteBackups(BatchDeleteBackup bdb) {
        if (!UserContext.permitAllow(bdb.getUsername())) {
            throw new UnsupportedOperationException(I18nUtils.getMessage(context, "BackupServiceImpl.msg1"));
        }
        for (BackupEntity backup : bdb.getBackups()) {
            if (!UserContext.get().isAdmin() && !UserContext.sameUser(backup.getUsername())) {
                throw new UnsupportedOperationException(I18nUtils.getMessage(context, "BackupServiceImpl.deleteBackups.msg1"));
            }
        }
        target.deleteBackups(bdb);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public synchronized void recoveryBackup(BackupEntity bk) {
        rbac(bk);
        target.recoveryBackup(bk);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public synchronized void recoveryBackups(BatchDeleteRecovery bdr) {
        if (!UserContext.permitAllow(bdr.getUsername())) {
            throw new UnsupportedOperationException(I18nUtils.getMessage(context, "BackupServiceImpl.msg1"));
        }
        target.recoveryBackups(bdr);
    }

    @Override
    public List<ProfileEntity> queryProfileList() {
        return target.queryProfileList();
    }

    private void rbac(BackupEntity bk) {
        String currentUserName = UserContext.get().getUserName();
        if (UserContext.get().isAdmin() || UserContext.permitAllow(bk.getUsername())
                && serviceDao.querySelectCountByUser(bk.getServiceId(), currentUserName) > 0) {
        } else {
            throw new UnsupportedOperationException(I18nUtils.getMessage(context, "BackupServiceImpl.msg1"));
        }
    }
}
