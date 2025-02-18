package com.github.hbq969.middleware.dbc.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Pair;
import com.github.hbq969.code.common.spring.context.SpringContext;
import com.github.hbq969.code.common.utils.*;
import com.github.hbq969.code.dict.service.api.impl.MapDictHelperImpl;
import com.github.hbq969.code.sm.login.session.UserContext;
import com.github.hbq969.middleware.dbc.dao.ConfigDao;
import com.github.hbq969.middleware.dbc.dao.ProfileDao;
import com.github.hbq969.middleware.dbc.dao.entity.ConfigEntity;
import com.github.hbq969.middleware.dbc.dao.entity.ConfigFileEntity;
import com.github.hbq969.middleware.dbc.dao.entity.ConfigProfileEntity;
import com.github.hbq969.middleware.dbc.dao.entity.ServiceConfigEntity;
import com.github.hbq969.middleware.dbc.model.AccountServiceProfile;
import com.github.hbq969.middleware.dbc.service.BackupService;
import com.github.hbq969.middleware.dbc.service.ConfigService;
import com.github.hbq969.middleware.dbc.view.request.ConfigProfileQuery;
import com.github.hbq969.middleware.dbc.view.request.DeleteConfigMultiple;
import com.github.hbq969.middleware.dbc.view.request.DownFile;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
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

    public final static String sqlSave = "insert into h_dbc_config(app,username,service_id,profile_name,config_key,config_value,created_at) values(?,?,?,?,?,?,?)";
    public final static String sqlUpdate = "update h_dbc_config set config_value=?,updated_at=? where app=? and username=? and service_id=? and profile_name=? and config_key=?";

    @Autowired
    private ConfigDao configDao;

    @Autowired
    private SpringContext context;

    @Autowired
    private MapDictHelperImpl dict;

    @Autowired
    private FileReaderFacade fileReader;

    @Autowired
    @Qualifier("dbc-BackupProxyImpl")
    private BackupService backupService;

    @Override
    public PageInfo<ConfigProfileEntity> queryConfigProfileList(ConfigProfileQuery q, int pageNum, int pageSize) {
        q.userInitial(context);
        if (UserContext.permitAllow(q.getUsername())) {
            PageInfo<ConfigProfileEntity> pg = PageHelper.startPage(pageNum, pageSize)
                    .doSelectPageInfo(() -> configDao.queryConfigProfileList(q));
            pg.getList().forEach(e -> e.convertDict(context));
            return pg;
        } else {
            throw new UnsupportedOperationException("账号无此操作权限");
        }
    }

    @Override
    public void saveConfig(AccountServiceProfile asp, ConfigEntity config) {
        if (UserContext.permitAllow(asp.getUsername())) {
            asp.userInitial(context);
            config.setCreatedAt(FormatTime.nowSecs());
            configDao.saveConfig(asp, config);
        } else {
            throw new UnsupportedOperationException("账号无此操作权限");
        }
    }

    @Override
    public void updateConfig(AccountServiceProfile asp, ConfigEntity config) {
        if (UserContext.permitAllow(asp.getUsername())) {
            config.setUpdatedAt(FormatTime.nowSecs());
            asp.userInitial(context);
            configDao.updateConfig(asp, config);
        } else {
            throw new UnsupportedOperationException("账号无此操作权限");
        }
    }

    @Override
    public void batchUpdateConfig(List<ServiceConfigEntity> rows) {
        Assert.notNull(rows, "批量更新的配置为空");
        String sql = "update h_dbc_config set config_value=? where app=? and username=? and service_id=? and profile_name=? and config_key=?";
        String app = context.getProperty("spring.application.name");
        log.info("批量更新配置, {}, [username,serviceId,profileName,configKey,configValue] => {}",
                sql, rows.stream()
                        .map(r -> String.join(",", r.getUsername(), r.getServiceId(), r.getProfileName(), r.getConfigKey(), r.getConfigValue()))
                        .collect(Collectors.toList()));
        context.getBean(JdbcTemplate.class).batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ServiceConfigEntity row = rows.get(i);
                ps.setString(1, row.getConfigValue());
                ps.setString(2, app);
                ps.setString(3, UserContext.get().isAdmin() ? row.getUsername() : UserContext.get().getUserName());
                ps.setString(4, row.getServiceId());
                ps.setString(5, row.getProfileName());
                ps.setString(6, row.getConfigKey());
            }

            @Override
            public int getBatchSize() {
                return rows.size();
            }
        });
    }

    @Override
    public void deleteConfig(AccountServiceProfile asp, ConfigEntity q) {
        if (UserContext.permitAllow(asp.getUsername())) {
            asp.userInitial(context);
            configDao.deleteConfig(asp, q);
        } else {
            throw new UnsupportedOperationException("账号无此操作权限");
        }
    }

    @Override
    public void batchDeleteConfig(List<ServiceConfigEntity> rows) {
        Assert.notNull(rows, "批量更新的配置为空");
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
                ps.setString(2, UserContext.get().isAdmin() ? row.getUsername() : UserContext.get().getUserName());
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

    @Override
    public void deleteConfigMultiple(DeleteConfigMultiple dcm) {
        if (UserContext.permitAllow(dcm.getAsp().getUsername())) {
            dcm.getAsp().userInitial(context);
            deleteConfigMultiple(dcm, true);
        } else {
            throw new UnsupportedOperationException("账号无此操作权限");
        }
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
        if (UserContext.permitAllow(asp.getUsername())) {
            PageInfo<ConfigEntity> pg = PageHelper.startPage(pageNum, pageSize)
                    .doSelectPageInfo(() -> configDao.queryConfigList(asp, q));
            pg.getList().forEach(e -> e.convertDict(context));
            return pg;
        } else {
            throw new UnsupportedOperationException("账号无此操作权限");
        }
    }

    @Override
    public void configImport(AccountServiceProfile asp, MultipartFile file, String cover, String backup) {
        asp.userInitial(context);
        if (!UserContext.permitAllow(asp.getUsername())) {
            throw new UnsupportedOperationException("账号无此操作权限");
        }
        backupService.backupOnConfigImport(asp);
        String fileType = FileUtil.getSuffix(file.getOriginalFilename());
        if (!dict.isDictKey("import,file,type", fileType)) {
            throw new UnsupportedOperationException(String.format("不支持的导入文件类型: %s", fileType));
        }
        List<Pair<String, Object>> pairs = null;
        try {
            pairs = fileReader.getService(fileType).read(file.getInputStream());
        } catch (IOException e) {
            log.error(String.format("读取导入文件 %s 异常", file.getOriginalFilename()), e);
            throw new RuntimeException(e);
        }
        batchConfigs(asp, cover, pairs);
    }

    private void batchConfigs(AccountServiceProfile asp, String cover, List<Pair<String, Object>> pairs) {
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
        if (UserContext.permitAllow(asp.getUsername())) {
            List<ConfigEntity> cfes = configDao.queryConfigList(asp, new ConfigEntity());
            List<Pair<String, Object>> paris = cfes.stream()
                    .map(c -> new Pair<String, Object>(c.getConfigKey(), c.getConfigValue()))
                    .collect(Collectors.toList());
            ConfigFileEntity result = new ConfigFileEntity();
            String fileContent = YamlPropertiesFileConverter.propertiesToYaml(paris);
            result.setFileContent(fileContent);
            return result;
        } else {
            throw new UnsupportedOperationException("账号无此操作权限");
        }
    }

    @Override
    public void updateConfigFile(ConfigFileEntity cfe) {
        cfe.setApp(context.getProperty("spring.application.name"));
        if (!UserContext.permitAllow(cfe.getUsername())) {
            throw new UnsupportedOperationException("账号无此操作权限");
        }
        cfe.setUpdatedAt(FormatTime.nowSecs());
        backupService.backupOnUpdateConfigFile(cfe);
        // 比较和properties的差异
        List<Pair<String, Object>> yamlParis = YamlPropertiesFileConverter.yamlToProperties(cfe.getFileContent());
        Map<String, Pair<String, Object>> yamlPairMap = yamlParis.stream()
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
        List<Pair<String, Object>> addList = new ArrayList<>(20);
        Pair<String, Object> tpair;
        Iterator<Pair<String, Object>> it2 = yamlParis.iterator();
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
        List<Pair<String, Object>> updateList = new ArrayList<>();
        for (ConfigEntity configEntity : removeList) {
            propertiesConfigMap.remove(configEntity.getConfigKey());
        }
        for (Pair<String, Object> pair : addList) {
            yamlPairMap.remove(pair.getKey());
        }
        for (Map.Entry<String, Pair<String, Object>> entry : yamlPairMap.entrySet()) {
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
        SubList<Pair<String, Object>> sub = new SubList<>();
        sub.setList(updateList);
        batchUpdateConfig(asp, sub, sqlUpdate);

    }

    @Override
    public void downFile(HttpServletResponse response, DownFile downFile) {
        try {
            downFile.userInitial(context);
            if (!UserContext.permitAllow(downFile.getUsername())) {
                throw new UnsupportedOperationException("账号无此操作权限");
            }
            String filename = downFile.getFilename();
            AccountServiceProfile asp = downFile.propertySet();
            List<ConfigEntity> cfs = configDao.queryConfigList(asp, new ConfigEntity());
            List<Pair<String, Object>> pairs = cfs.stream()
                    .map(c -> new Pair<String, Object>(c.getConfigKey(), c.getConfigValue()))
                    .collect(Collectors.toList());
            String yamlContent = YamlPropertiesFileConverter.propertiesToYaml(pairs);
            if (StringUtils.equals("yml", downFile.getFileSuffix())) {
                response.setHeader("Content-disposition", "attachment; filename=" + filename);
                response.setContentType("application/yaml");
                FileCopyUtils.copy(new StringReader(yamlContent), response.getWriter());
            } else if (StringUtils.equals("properties", downFile.getFileSuffix())) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                for (Pair<String, Object> pair : pairs) {
                    out.write(String.join(": ", pair.getKey(), String.valueOf(pair.getValue())).getBytes(StandardCharsets.UTF_8));
                    out.write("\n".getBytes(StandardCharsets.UTF_8));
                }
                FileCopyUtils.copy(out.toByteArray(), response.getOutputStream());
            } else {
                throw new UnsupportedOperationException("不支持的文件类型");
            }
        } catch (Exception e) {
            log.error("下载文件异常", e);
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

    private void batchUpdateConfig(AccountServiceProfile asp, SubList<Pair<String, Object>> data, String sqlUpdate) {
        context.getBean(JdbcTemplate.class).batchUpdate(sqlUpdate, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Pair<String, Object> pair = data.getList().get(i);
                ps.setObject(1, pair.getValue());
                ps.setLong(2, FormatTime.nowSecs());
                ps.setString(3, asp.getApp());
                ps.setString(4, asp.getUsername());
                ps.setString(5, asp.getServiceId());
                ps.setString(6, asp.getProfileName());
                ps.setString(7, pair.getKey());
            }

            @Override
            public int getBatchSize() {
                return data.getList().size();
            }
        });
        log.info("批量覆盖配置项: {}", data.getList().size());
    }

    private void batchSaveConfig(AccountServiceProfile asp, SubList<Pair<String, Object>> data, String sqlSave) {
        try {
            context.getBean(JdbcTemplate.class).batchUpdate(sqlSave, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    Pair<String, Object> pair = data.getList().get(i);
                    ps.setString(1, asp.getApp());
                    ps.setString(2, asp.getUsername());
                    ps.setString(3, asp.getServiceId());
                    ps.setString(4, asp.getProfileName());
                    ps.setString(5, pair.getKey());
                    ps.setObject(6, pair.getValue());
                    ps.setLong(7, FormatTime.nowSecs());
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
            for (Pair<String, Object> pair : data.getList()) {
                try {
                    context.getBean(JdbcTemplate.class).update(sqlSave, ps -> {
                        ps.setString(1, asp.getApp());
                        ps.setString(2, asp.getUsername());
                        ps.setString(3, asp.getServiceId());
                        ps.setString(4, asp.getProfileName());
                        ps.setString(5, pair.getKey());
                        ps.setObject(6, pair.getValue());
                        ps.setLong(7, FormatTime.nowSecs());
                    });
                    c.incrementAndGet();
                } catch (DataAccessException ex) {
                }
            }
            log.info("单挑保存配置项成功 {}", c.intValue());
        }
    }
}

