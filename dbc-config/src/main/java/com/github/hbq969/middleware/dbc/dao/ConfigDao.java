package com.github.hbq969.middleware.dbc.dao;

import com.github.hbq969.middleware.dbc.dao.entity.ConfigEntity;
import com.github.hbq969.middleware.dbc.dao.entity.ConfigProfileEntity;
import com.github.hbq969.middleware.dbc.model.AccountServiceProfile;
import com.github.hbq969.middleware.dbc.view.request.ConfigProfileQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ConfigDao {
    List<ConfigProfileEntity> queryConfigProfileList(ConfigProfileQuery q);

    void saveConfig(@Param("asp") AccountServiceProfile asp, @Param("config") ConfigEntity config);

    void updateConfig(@Param("asp") AccountServiceProfile asp, @Param("config") ConfigEntity config);

    void deleteConfig(@Param("asp") AccountServiceProfile asp, @Param("q") ConfigEntity config);

    List<ConfigEntity> queryConfigList(@Param("asp") AccountServiceProfile asp, @Param("q") ConfigEntity q);
}
