package com.github.hbq969.middleware.dbc.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Assert;
import com.github.hbq969.code.common.spring.context.SpringContext;
import com.github.hbq969.code.common.spring.yaml.TypePair;
import com.github.hbq969.code.common.utils.SubList;
import com.github.hbq969.code.common.utils.*;
import com.github.hbq969.code.dict.service.api.impl.MapDictHelperImpl;
import com.github.hbq969.code.sm.login.session.UserContext;
import com.github.hbq969.middleware.dbc.dao.BackupDao;
import com.github.hbq969.middleware.dbc.dao.ConfigDao;
import com.github.hbq969.middleware.dbc.dao.ServiceDao;
import com.github.hbq969.middleware.dbc.dao.entity.*;
import com.github.hbq969.middleware.dbc.model.AccountServiceProfile;
import com.github.hbq969.middleware.dbc.model.CurrentAndBackupFile;
import com.github.hbq969.middleware.dbc.service.BackupService;
import com.github.hbq969.middleware.dbc.service.ConfigService;
import com.github.hbq969.middleware.dbc.view.request.CompareCurrentAndBackupFile;
import com.github.hbq969.middleware.dbc.view.request.ConfigProfileQuery;
import com.github.hbq969.middleware.dbc.view.request.DeleteConfigMultiple;
import com.github.hbq969.middleware.dbc.view.request.DownFile;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Service("dbc-ConfigServiceImpl")
@Slf4j
public class ConfigServiceImpl implements ConfigService {

    public final static String sqlSave = "insert into h_dbc_config(app,username,service_id,profile_name,config_key,config_value,data_type,created_at) values(?,?,?,?,?,?,?,?)";
    public final static String sqlUpdate = "update h_dbc_config set config_value=?,data_type=?,updated_at=? where app=? and username=? and service_id=? and profile_name=? and config_key=?";

    @Autowired
    private ConfigDao configDao;

    @Autowired
    private SpringContext context;

    @Autowired
    private MapDictHelperImpl dict;

    @Autowired
    private FileReaderFacade fileReader;

    @Autowired
    @Qualifier("dbc-BackupServiceRBACImpl")
    private BackupService backupService;

    @Autowired
    private BackupDao backupDao;
    @Qualifier("dbc-ServiceDao")
    @Autowired
    private ServiceDao serviceDao;

    @Override
    public PageInfo<ConfigProfileEntity> queryConfigProfileList(ConfigProfileQuery q, int pageNum, int pageSize) {
        q.userInitial(context);
        PageInfo<ConfigProfileEntity> pg = PageHelper.startPage(pageNum, pageSize)
                .doSelectPageInfo(() -> configDao.queryConfigProfileList(q));
        pg.getList().forEach(e -> e.convertDict(context));
        return pg;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveConfig(AccountServiceProfile asp, ConfigEntity config) {
        asp.userInitial(context);
        config.setCreatedAt(FormatTime.nowSecs());
        configDao.saveConfig(asp, config);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateConfig(AccountServiceProfile asp, ConfigEntity config) {
        config.setUpdatedAt(FormatTime.nowSecs());
        asp.userInitial(context);
        configDao.updateConfig(asp, config);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void batchUpdateConfig(List<ServiceConfigEntity> rows) {
        Assert.notNull(rows, I18nUtils.getMessage(context, "ConfigServiceImpl.batchUpdateConfig.msg1"));
        String sql = "update h_dbc_config set config_value=?, data_type=?, updated_at=? where app=? and username=? and service_id=? and profile_name=? and config_key=?";
        String app = context.getProperty("spring.application.name");
        log.info("批量更新配置, {}, [username,serviceId,profileName,configKey,configValue,dataType] => {}",
                sql, rows.stream()
                        .map(r -> String.join(",", r.getUsername(), r.getServiceId(), r.getProfileName(), r.getConfigKey(), r.getConfigValue(), r.getDataType()))
                        .collect(Collectors.toList()));
        long updatedAt = FormatTime.nowSecs();
        context.getBean(JdbcTemplate.class).batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ServiceConfigEntity row = rows.get(i);
                ps.setString(1, row.getConfigValue());
                ps.setString(2, row.getDataType());
                ps.setLong(3, updatedAt);
                ps.setString(4, app);
                ps.setString(5, row.getUsername());
                ps.setString(6, row.getServiceId());
                ps.setString(7, row.getProfileName());
                ps.setString(8, row.getConfigKey());
            }

            @Override
            public int getBatchSize() {
                return rows.size();
            }
        });
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteConfig(AccountServiceProfile asp, ConfigEntity q) {
        asp.userInitial(context);
        configDao.deleteConfig(asp, q);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void batchDeleteConfig(List<ServiceConfigEntity> rows) {
        Assert.notNull(rows, I18nUtils.getMessage(context, "ConfigServiceImpl.batchUpdateConfig.msg1"));
        String currentUserName = UserContext.get().getUserName();
        String sql = "delete from h_dbc_config where app=? and username=? and service_id=? and profile_name=? and config_key=?";
        String app = context.getProperty("spring.application.name");
        log.info("批量删除配置, {}, [username,serviceId,profileName,configKey] => {}",
                sql, rows.stream()
                        .map(r -> String.join(",", r.getUsername(), r.getServiceId(), r.getProfileName(), r.getConfigKey()))
                        .collect(Collectors.toList()));
        context.getBean(JdbcTemplate.class).batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ServiceConfigEntity row = rows.get(i);
                ps.setString(1, app);
                ps.setString(2, row.getUsername());
                ps.setString(3, row.getServiceId());
                ps.setString(4, row.getProfileName());
                ps.setString(5, row.getConfigKey());
            }

            @Override
            public int getBatchSize() {
                return rows.size();
            }
        });
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteConfigMultiple(DeleteConfigMultiple dcm) {
        dcm.getAsp().userInitial(context);
        deleteConfigMultiple(dcm, true);
    }

    private void deleteConfigMultiple(DeleteConfigMultiple dcm, boolean fromPage) {
        dcm.getAsp().userInitial(context);
        DigitSplit.defaultStep(200).split(dcm.getConfigKeys()).forEach(sub -> {
            String sql = "delete from h_dbc_config where app=? and username=? and service_id=? and profile_name=? and config_key=?";
            log.info("批量删除配置, {}, {} 个", sql, sub.getList().size());
            context.getBean(JdbcTemplate.class).batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    String configKey = sub.getList().get(i);
                    ps.setString(1, dcm.getAsp().getApp());
                    ps.setString(2, dcm.getAsp().getUsername());
                    ps.setString(3, dcm.getAsp().getServiceId());
                    ps.setString(4, dcm.getAsp().getProfileName());
                    ps.setString(5, configKey);
                }

                @Override
                public int getBatchSize() {
                    return sub.getList().size();
                }
            });
        });
    }

    @Override
    public PageInfo<ConfigEntity> queryConfigList(AccountServiceProfile asp, ConfigEntity q, int pageNum, int pageSize) {
        asp.setApp(context.getProperty("spring.application.name"));
        PageInfo<ConfigEntity> pg = PageHelper.startPage(pageNum, pageSize)
                .doSelectPageInfo(() -> configDao.queryConfigList(asp, q));
        pg.getList().forEach(e -> e.convertDict(context));
        return pg;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void configImport(AccountServiceProfile asp, MultipartFile file, String cover, String backup) {
        asp.userInitial(context);
        if (StringUtils.equals(backup, "Y")) {
            backupService.backupOnConfigImport(asp);
        }
        String fileType = FileUtil.getSuffix(file.getOriginalFilename());
        if (!dict.isDictKey("import,file,type", fileType)) {
            throw new UnsupportedOperationException(String.format("%s: %s", I18nUtils.getMessage(context, "ConfigServiceImpl.configImport.msg1"), fileType));
        }
        List<TypePair> pairs = null;
        try {
            pairs = fileReader.getService(fileType).read(file.getInputStream());
        } catch (IOException e) {
            log.error(String.format("读取导入文件 %s 异常", file.getOriginalFilename()), e);
            throw new RuntimeException(e);
        }
        batchConfigs(asp, cover, pairs);
    }

    private void batchConfigs(AccountServiceProfile asp, String cover, List<TypePair> pairs) {
        DigitSplit.defaultStep(200).split(pairs).forEach(data -> {
            batchSaveConfig(asp, data, sqlSave);
            if (StringUtils.equals("Y", cover)) {
                batchUpdateConfig(asp, data, sqlUpdate);
            }
        });
    }

    @Override
    public ConfigFileEntity queryConfigFile(AccountServiceProfile asp) {
        asp.setApp(context.getProperty("spring.application.name"));
        List<ConfigEntity> cfes = configDao.queryConfigList(asp, new ConfigEntity());
        List<TypePair> paris = cfes.stream()
                .map(c -> new TypePair(c.getConfigKey(), c.getConfigValue(), c.getDataType()))
                .collect(Collectors.toList());
        ConfigFileEntity result = new ConfigFileEntity();
        String fileContent = YamlPropertiesFileConverter.propertiesToYaml(paris);
        result.setFileContent(fileContent);
        return result;
    }

    @Override
    public void updateConfigFile(ConfigFileEntity cfe) {
        cfe.setApp(context.getProperty("spring.application.name"));
        cfe.setUpdatedAt(FormatTime.nowSecs());
        if (cfe.ifBackup()) {
            backupService.backupOnUpdateConfigFile(cfe);
        }
        // 比较和properties的差异
        List<TypePair> yamlParis = YamlPropertiesFileConverter.yamlToProperties(cfe.getFileContent());
        Map<String, TypePair> yamlPairMap = yamlParis.stream()
                .collect(Collectors.toMap(p -> p.getKey(), p -> p, (k1, k2) -> k2));
        AccountServiceProfile asp = new AccountServiceProfile().propertySet(cfe);

        List<ConfigEntity> propertiesConfigs = configDao.queryConfigList(asp, new ConfigEntity());
        Map<String, ConfigEntity> propertiesConfigMap = propertiesConfigs.stream()
                .collect(Collectors.toMap(c -> c.getConfigKey(), c -> c, (k1, k2) -> k2));

        List<ConfigEntity> removeList = new ArrayList<>(20);
        ConfigEntity tce;
        // 1.删除propertiesConfigs多出来的配置
        Iterator<ConfigEntity> it = propertiesConfigs.iterator();
        while (it.hasNext()) {
            tce = it.next();
            if (!yamlPairMap.containsKey(tce.getConfigKey())) {
                log.debug("删除propertiesConfigs多出来的配置: {}", tce.getConfigKey());
                removeList.add(tce);
                it.remove();
            }
        }
        DeleteConfigMultiple dcm = new DeleteConfigMultiple();
        dcm.setConfigKeys(removeList.stream().map(c -> c.getConfigKey()).collect(Collectors.toList()));
        dcm.setAsp(asp);
        deleteConfigMultiple(dcm, false);

        // 2.添加yamlParis多出来的配置
        List<TypePair> addList = new ArrayList<>(20);
        TypePair tpair;
        Iterator<TypePair> it2 = yamlParis.iterator();
        while (it2.hasNext()) {
            tpair = it2.next();
            if (!propertiesConfigMap.containsKey(tpair.getKey())) {
                log.debug("添加yamlParis多出来的配置: {}", tpair.getKey());
                addList.add(tpair);
                it2.remove();
            }
        }
        batchConfigs(asp, "Y", addList);

        // 3.比较剩余的配置值是否有变化，更新之
        List<TypePair> updateList = new ArrayList<>();
        for (ConfigEntity configEntity : removeList) {
            propertiesConfigMap.remove(configEntity.getConfigKey());
        }
        for (TypePair pair : addList) {
            yamlPairMap.remove(pair.getKey());
        }
        for (Map.Entry<String, TypePair> entry : yamlPairMap.entrySet()) {
            tce = propertiesConfigMap.get(entry.getKey());
            if (tce == null) {
                continue;
            }
            if (!StringUtils.equals(String.valueOf(entry.getValue().getValue()), tce.getConfigValue())) {
                log.debug("更新yamlParis和propertiesConfigs差异的配置，key: {}, yaml值: {}, properties值: {}",
                        tce.getConfigKey(), entry.getValue(), tce.getConfigValue());
                updateList.add(entry.getValue());
            }
        }
        SubList<TypePair> sub = new SubList<>();
        sub.setList(updateList);
        batchUpdateConfig(asp, sub, sqlUpdate);
    }

    @Override
    public void downFile(HttpServletResponse response, DownFile downFile) {
        try {
            downFile.userInitial(context);
            String currentUserName = UserContext.get().getUserName();
            if (UserContext.get().isAdmin() || UserContext.permitAllow(downFile.getUsername())
                    && serviceDao.querySelectCountByUser(downFile.getServiceId(), currentUserName) > 0) {
                String filename = downFile.getFilename();
                AccountServiceProfile asp = downFile.propertySet();
                List<ConfigEntity> cfs = configDao.queryConfigList(asp, new ConfigEntity());
                List<TypePair> pairs = cfs.stream()
                        .map(c -> new TypePair(c.getConfigKey(), c.getConfigValue(), c.getDataType()))
                        .collect(Collectors.toList());
                String yamlContent = YamlPropertiesFileConverter.propertiesToYaml(pairs);
                if (StringUtils.equals("yml", downFile.getFileSuffix())) {
                    response.setHeader("Content-disposition", "attachment; filename=" + filename);
                    response.setContentType("application/yaml;charset=utf-8");
                    FileCopyUtils.copy(new StringReader(yamlContent), response.getWriter());
                } else if (StringUtils.equals("properties", downFile.getFileSuffix())) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    for (TypePair pair : pairs) {
                        out.write(String.join(": ", pair.getKey(), String.valueOf(pair.getValue())).getBytes(StandardCharsets.UTF_8));
                        out.write("\n".getBytes(StandardCharsets.UTF_8));
                    }
                    FileCopyUtils.copy(out.toByteArray(), response.getOutputStream());
                } else {
                    throw new UnsupportedOperationException(I18nUtils.getMessage(context, "ConfigServiceImpl.downFile.msg1"));
                }
            } else {
                throw new UnsupportedOperationException(I18nUtils.getMessage(context, "BackupServiceImpl.msg1"));
            }
        } catch (Exception e) {
            log.error("下载文件异常", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void downBootstrapFile(HttpServletResponse response, Map map) {
        String filename = MapUtils.getString(map, "filename");
        String serviceName = MapUtils.getString(map, "serviceName");
        String profileName = MapUtils.getString(map, "profileName");
        Map<String, Object> prop = new LinkedHashMap<>();
        prop.put("spring.cloud.config.h-dbc.enabled", true);
        prop.put("spring.cloud.config.h-dbc.dbc-key", context.getProperty("spring.application.name"));
        prop.put("spring.cloud.config.h-dbc.service-name", serviceName);
        prop.put("spring.cloud.config.h-dbc.profile-name", profileName);
        prop.put("spring.cloud.config.h-dbc.api.charset", "utf-8");
        prop.put("spring.cloud.config.h-dbc.api.api-log", false);
        prop.put("spring.cloud.config.h-dbc.api.url", "http://localhost:30170/h-dbc");
        prop.put("spring.cloud.config.h-dbc.api.basic-auth.username", "api authentication account");
        prop.put("spring.cloud.config.h-dbc.api.basic-auth.password", "api authentication password");
        List<TypePair> pairs = new ArrayList<>(prop.size());
        for (Map.Entry<String, Object> e : prop.entrySet()) {
            TypePair pair = new TypePair(e.getKey(), e.getValue(), e.getValue().getClass().getName());
            pairs.add(pair);
        }
        String yaml = YamlPropertiesFileConverter.propertiesToYaml(pairs);
        response.setHeader("Content-disposition", "attachment; filename=" + filename);
        response.setContentType("application/yaml");
        try {
            FileCopyUtils.copy(new StringReader(yaml), response.getWriter());
            response.flushBuffer();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ServiceConfigEntity> queryAllProfilesThisConfig(Map map) {
        map.put("app", context.getProperty("spring.application.name"));
        map.put("admin", UserContext.get().isAdmin());
        map.put("username", UserContext.get().getUserName());
        List<ServiceConfigEntity> list = configDao.queryAllProfilesThisConfig(map);
        list.forEach(e -> e.convertDict(context));
        return list;
    }

    @Override
    public void backup(AccountServiceProfile asp) {
        backupService.backupOnConfigImport(asp);
    }

    @Override
    public CurrentAndBackupFile getCurrentAndBackupFile(CompareCurrentAndBackupFile ccabf) {
        CurrentAndBackupFile cabf = new CurrentAndBackupFile();
        ConfigFileEntity cfe = queryConfigFile(ccabf.getCurrent());
        cabf.setCurrentFileContent(cfe.getFileContent());
        BackupEntity q = new BackupEntity();
        q.setId(ccabf.getBackupId());
        BackupEntity be = backupDao.queryBackup(q);
        List<TypePair> bkPairs = GsonUtils.parseArray(be.getBackupContent(), new TypeToken<List<ConfigEntity>>() {
                }).stream()
                .map(c -> new TypePair(c.getConfigKey(), c.getConfigValue(), c.getDataType()))
                .collect(Collectors.toList());
        String fileContent2 = YamlPropertiesFileConverter.propertiesToYaml(bkPairs);
        cabf.setBackupFileContent(fileContent2);
        return cabf;
    }

    private void batchUpdateConfig(AccountServiceProfile asp, SubList<TypePair> data, String sqlUpdate) {
        context.getBean(JdbcTemplate.class).batchUpdate(sqlUpdate, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                TypePair pair = data.getList().get(i);
                ps.setString(1, String.valueOf(pair.getValue()));
                ps.setString(2, pair.getType().getTypeName());
                ps.setLong(3, FormatTime.nowSecs());
                ps.setString(4, asp.getApp());
                ps.setString(5, asp.getUsername());
                ps.setString(6, asp.getServiceId());
                ps.setString(7, asp.getProfileName());
                ps.setString(8, pair.getKey());
            }

            @Override
            public int getBatchSize() {
                return data.getList().size();
            }
        });
        log.info("批量覆盖配置项: {}", data.getList().size());
    }

    private void batchSaveConfig(AccountServiceProfile asp, SubList<TypePair> data, String sqlSave) {
        try {
            context.getBean(JdbcTemplate.class).batchUpdate(sqlSave, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    TypePair pair = data.getList().get(i);
                    ps.setString(1, asp.getApp());
                    ps.setString(2, asp.getUsername());
                    ps.setString(3, asp.getServiceId());
                    ps.setString(4, asp.getProfileName());
                    ps.setString(5, pair.getKey());
                    ps.setString(6, String.valueOf(pair.getValue()));
                    ps.setString(7, pair.getType().getTypeName());
                    ps.setLong(8, FormatTime.nowSecs());
                }

                @Override
                public int getBatchSize() {
                    return data.getList().size();
                }
            });
            log.info("批量保存配置项: {}", data.getList().size());
        } catch (DataAccessException e) {
            log.info("批量保存配置项失败: {}", data.getList().size());
            Count c = Count.unsafe();
            for (TypePair pair : data.getList()) {
                try {
                    context.getBean(JdbcTemplate.class).update(sqlSave, ps -> {
                        ps.setString(1, asp.getApp());
                        ps.setString(2, asp.getUsername());
                        ps.setString(3, asp.getServiceId());
                        ps.setString(4, asp.getProfileName());
                        ps.setString(5, pair.getKey());
                        ps.setString(6, String.valueOf(pair.getValue()));
                        ps.setString(7, pair.getType().getTypeName());
                        ps.setLong(8, FormatTime.nowSecs());
                    });
                    c.incrementAndGet();
                } catch (DataAccessException ex) {
                }
            }
            log.info("单挑保存配置项成功 {}", c.intValue());
        }
    }
}

