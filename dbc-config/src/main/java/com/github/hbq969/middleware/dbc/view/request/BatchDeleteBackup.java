package com.github.hbq969.middleware.dbc.view.request;

import cn.hutool.core.lang.Assert;
import com.github.hbq969.code.sm.login.session.UserContext;
import com.github.hbq969.middleware.dbc.dao.entity.BackupEntity;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Data
public class BatchDeleteBackup {
    private String username;
    private List<BackupEntity> backups;

    public void check() {
        Assert.notNull(username, "账号不能为空");
        Assert.notNull(backups, "批量备份记录不能为空");
        for (BackupEntity backup : backups) {
            if (!UserContext.get().isAdmin() && !StringUtils.equals(backup.getUsername(), username)) {
                throw new IllegalArgumentException("传入的非本账号创建的备份记录，请检查");
            }
        }
    }
}
