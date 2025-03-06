package com.github.hbq969.middleware.dbc.view.request;

import cn.hutool.core.lang.Assert;
import com.github.hbq969.code.common.spring.context.SpringContext;
import com.github.hbq969.code.common.utils.I18nUtils;
import com.github.hbq969.code.sm.login.session.UserContext;
import com.github.hbq969.middleware.dbc.dao.entity.BackupEntity;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Data
public class BatchDeleteRecovery {
    private String username;
    private List<BackupEntity> recoveries;

    public void check(SpringContext context) {
        Assert.notNull(username, I18nUtils.getMessage(context, "BatchDeleteRecovery.message1"));
        Assert.notNull(recoveries, I18nUtils.getMessage(context, "BatchDeleteRecovery.message2"));
        for (BackupEntity backup : recoveries) {
            if (!UserContext.get().isAdmin() && !StringUtils.equals(backup.getUsername(), username)) {
                throw new IllegalArgumentException(I18nUtils.getMessage(context, "BatchDeleteRecovery.message3"));
            }
        }
    }
}
