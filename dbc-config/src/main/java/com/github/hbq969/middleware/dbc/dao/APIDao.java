package com.github.hbq969.middleware.dbc.dao;

import com.github.hbq969.middleware.dbc.dao.entity.ConfigEntity;
import com.github.hbq969.middleware.dbc.model.APIModel;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("dbc-APIDao")
@Mapper
public interface APIDao {
    List<ConfigEntity> queryConfigList(APIModel model);
}
