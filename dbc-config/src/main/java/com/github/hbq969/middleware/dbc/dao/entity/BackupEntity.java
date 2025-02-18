package com.github.hbq969.middleware.dbc.dao.entity;

import com.github.hbq969.code.common.spring.context.SpringContext;
import com.github.hbq969.code.common.utils.FormatTime;
import com.github.hbq969.code.dict.service.api.DictAware;
import com.github.hbq969.code.dict.service.api.DictModel;
import lombok.Data;

@Data
public class BackupEntity implements DictAware, DictModel {
    private String id;
    private String app;
    private String username;
    private String serviceId;
    private String serviceName;
    private String serviceDesc;
    private String profileName;
    private String profileDesc;
    private String filename;
    private String backupContent;
    private Long createdAt;
    private String fmtCreatedAt;

    @Override
    public void convertDict(SpringContext context) {
        DictAware.super.convertDict(context);
        if (createdAt != null) {
            this.fmtCreatedAt = FormatTime.YYYYMMDDHHMISS.withSecs(createdAt.longValue());
        }
    }
}
