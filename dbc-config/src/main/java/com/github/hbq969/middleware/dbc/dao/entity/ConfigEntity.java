package com.github.hbq969.middleware.dbc.dao.entity;

import com.github.hbq969.code.common.spring.context.SpringContext;
import com.github.hbq969.code.common.utils.FormatTime;
import com.github.hbq969.code.dict.service.api.DictAware;
import com.github.hbq969.code.dict.service.api.DictModel;
import lombok.Data;

@Data
public class ConfigEntity implements DictModel, DictAware {
    private String configKey;
    private String configValue;
    private Long createdAt;
    private Long updatedAt;
    private String fmtCreatedAt;
    private String fmtUpdatedAt;

    @Override
    public void convertDict(SpringContext context) {
        DictAware.super.convertDict(context);
        if (createdAt != null) {
            this.fmtCreatedAt = FormatTime.YYYYMMDDHHMISS.withSecs(createdAt.longValue());
        }
        if (updatedAt != null) {
            this.fmtUpdatedAt = FormatTime.YYYYMMDDHHMISS.withSecs(updatedAt.longValue());
        }
    }
}
