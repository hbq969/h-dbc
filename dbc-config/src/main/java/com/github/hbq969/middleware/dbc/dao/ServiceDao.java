package com.github.hbq969.middleware.dbc.dao;

import com.github.hbq969.middleware.dbc.dao.entity.ServiceEntity;
import com.github.hbq969.middleware.dbc.model.AccountService;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ServiceDao {
    void createService();

    void createAccService();

    void createProfiles();

    void createAccProfiles();

    void createConfig();

    void createConfigFile();

    void saveService(ServiceEntity service);

    void updateService(ServiceEntity service);

    void deleteServiceOnAdmin(ServiceEntity service);

    void deleteAccServiceOnAdmin(@Param("serviceId") String serviceId);

    void deleteServiceConfigOnAdmin(@Param("serviceId") String serviceId);

    void deleteServiceConfigFileOnAdmin(@Param("serviceId") String serviceId);

    void deleteService(AccountService as);

    void deleteAccService(AccountService as);

    void deleteServiceConfig(AccountService as);

    void deleteServiceConfigFile(AccountService as);


    List<ServiceEntity> queryServiceList(@Param("accService") AccountService accountService, @Param("service") ServiceEntity service);

    List<ServiceEntity> queryServiceByName(@Param("accService") AccountService accountService, @Param("serviceName") String serviceName);

    void saveAccountService(AccountService accountService);
}
