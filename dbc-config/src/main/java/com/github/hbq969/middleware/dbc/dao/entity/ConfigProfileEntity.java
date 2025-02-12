package com.github.hbq969.middleware.dbc.dao.entity;

import com.github.hbq969.code.common.spring.context.SpringContext;
import com.github.hbq969.code.common.utils.FormatTime;
import com.github.hbq969.code.dict.service.api.DictAware;
import com.github.hbq969.code.dict.service.api.DictModel;
import lombok.Data;

@Data
public class ConfigProfileEntity implements DictModel, DictAware {
    private String profileName;
    private String profileDesc;
    private int configNum;
    private Long lastUpdateTime;
    private String fmtLastUpdateTime;

    @Override
    public void convertDict(SpringContext context) {
        DictAware.super.convertDict(context);
        if (lastUpdateTime != null) {
            this.fmtLastUpdateTime = FormatTime.YYYYMMDDHHMISS.withSecs(lastUpdateTime.longValue());
        }
    }
}
