package com.github.hbq969.middleware.dbc.dao.entity;

import com.github.hbq969.code.common.spring.context.SpringContext;
import com.github.hbq969.code.common.utils.FormatTime;
import com.github.hbq969.code.dict.service.api.DictAware;
import com.github.hbq969.code.dict.service.api.DictModel;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class ServiceEntity implements DictAware, DictModel {
    private String serviceId;
    private String serviceName;
    private String serviceDesc;
    private Long createdAt;
    private Long updatedAt;
    private String fmtCreatedAt;
    private String fmtUpdatedAt;
    private String username;

    @Override
    public void convertDict(SpringContext context) {
        DictAware.super.convertDict(context);
        if(createdAt!=null){
            this.fmtCreatedAt= FormatTime.YYYYMMDDHHMISS.withSecs(createdAt.longValue());
        }
        if(updatedAt!=null){
            this.fmtUpdatedAt=FormatTime.YYYYMMDDHHMISS.withSecs(updatedAt.longValue());
        }
    }

    public boolean sameUser(String username){
        return StringUtils.equals(this.username,username);
    }
}
