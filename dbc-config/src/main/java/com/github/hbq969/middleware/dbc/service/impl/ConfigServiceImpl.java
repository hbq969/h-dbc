package com.github.hbq969.middleware.dbc.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Pair;
import com.github.hbq969.code.common.spring.context.SpringContext;
import com.github.hbq969.code.common.utils.*;
import com.github.hbq969.code.dict.service.api.impl.MapDictHelperImpl;
import com.github.hbq969.middleware.dbc.config.Config;
import com.github.hbq969.middleware.dbc.dao.ConfigDao;
import com.github.hbq969.middleware.dbc.dao.ProfileDao;
import com.github.hbq969.middleware.dbc.dao.entity.ConfigEntity;
import com.github.hbq969.middleware.dbc.dao.entity.ConfigFileEntity;
import com.github.hbq969.middleware.dbc.dao.entity.ConfigProfileEntity;
import com.github.hbq969.middleware.dbc.model.AccountServiceProfile;
import com.github.hbq969.middleware.dbc.service.ConfigService;
import com.github.hbq969.middleware.dbc.view.request.ConfigProfileQuery;
import com.github.hbq969.middleware.dbc.view.request.DeleteConfigMultiple;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.units.qual.C;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
    private ProfileDao profileDao;

    @Override
    public PageInfo<ConfigProfileEntity> queryConfigProfileList(ConfigProfileQuery q, int pageNum, int pageSize) {
        q.userInitial(context);
        PageInfo<ConfigProfileEntity> pg = PageHelper.startPage(pageNum, pageSize)
                .doSelectPageInfo(() -> configDao.queryConfigProfileList(q));
        pg.getList().forEach(e -> e.convertDict(context));
        return pg;
    }

    @Override
    public void saveConfig(AccountServiceProfile asp, ConfigEntity config) {
        asp.userInitial(context);
        config.setCreatedAt(FormatTime.nowSecs());
        configDao.saveConfig(asp, config);

        addOrUpdateConfigFile(asp, true);
    }

    @Override
    public void updateConfig(AccountServiceProfile asp, ConfigEntity config) {
        asp.userInitial(context);
        config.setUpdatedAt(FormatTime.nowSecs());
        configDao.updateConfig(asp, config);
        addOrUpdateConfigFile(asp, false);
    }

    @Override
    public void deleteConfig(AccountServiceProfile asp, ConfigEntity q) {
        asp.userInitial(context);
        configDao.deleteConfig(asp, q);
        addOrUpdateConfigFile(asp, false);
    }

    private void addOrUpdateConfigFile(AccountServiceProfile asp, boolean add) {
        ConfigFileEntity newFile = new ConfigFileEntity().propertySet(asp);
        ConfigFileEntity oldFile = configDao.queryConfigFile(newFile);

        List<ConfigEntity> allConfigs = configDao.queryConfigList(asp, new ConfigEntity());
        List<Pair<String, Object>> allPairs = allConfigs.stream()
                .map(c -> new Pair<String, Object>(c.getConfigKey(), c.getConfigValue()))
                .collect(Collectors.toList());
        String newYamlString = YamlPropertiesFileConverter.propertiesToYaml(allPairs);
        // 添加配置
        if (add) {
            newFile.setFileContent(newYamlString);
            // h_dbc_config_file未查询到记录，即未首次添加
            if (oldFile == null) {
                newFile.setCreatedAt(FormatTime.nowSecs());
                if (StringUtils.isEmpty(asp.getProfileName()) || StringUtils.equals("default", asp.getProfileName())) {
                    newFile.setFileName("application.yml");
                } else {
                    newFile.setFileName(String.format("application-%s.yml", asp.getProfileName()));
                }
                configDao.saveConfigFile(newFile);
            }
            // h_dbc_config_file查询到记录，增量添加
            else {
                newFile.setUpdatedAt(FormatTime.nowSecs());
                configDao.updateConfigFile(newFile);
            }
        }
        // 修改或删除配置
        else {
            // 如果一个配置都没了，就删除h_dbc_config_file记录
            if (StringUtils.isEmpty(newYamlString)) {
                configDao.deleteConfigFile(oldFile);
            }
            // 否则更新h_dbc_config_file
            else {
                oldFile.setUpdatedAt(FormatTime.nowSecs());
                oldFile.setFileContent(newYamlString);
                configDao.updateConfigFile(oldFile);
            }
        }
    }

    @Override
    public void deleteConfigMultiple(DeleteConfigMultiple dcm) {
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
        if (fromPage) {
            addOrUpdateConfigFile(dcm.getAsp(), false);
        }
    }

    @Override
    public PageInfo<ConfigEntity> queryConfigList(AccountServiceProfile asp, ConfigEntity q, int pageNum, int pageSize) {
        asp.userInitial(context);
        PageInfo<ConfigEntity> pg = PageHelper.startPage(pageNum, pageSize)
                .doSelectPageInfo(() -> configDao.queryConfigList(asp, q));
        pg.getList().forEach(e -> e.convertDict(context));
        return pg;
    }

    @Override
    public void configImport(AccountServiceProfile asp, MultipartFile file, String cover) {
        asp.userInitial(context);
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
        addOrUpdateConfigFile(asp, true);
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
        asp.userInitial(context);
        ConfigFileEntity cfe = new ConfigFileEntity().propertySet(asp);
        ConfigFileEntity result = configDao.queryConfigFile(cfe);
        if (result != null) {
            result.convertDict(context);
        }
        return result;
    }

    @Override
    public void updateConfigFile(ConfigFileEntity cfe) {
        cfe.userInitial(context);
        cfe.setUpdatedAt(FormatTime.nowSecs());
        ConfigFileEntity result = configDao.queryConfigFile(cfe);
        if (result == null) {
            configDao.saveConfigFile(cfe);
        } else {
            configDao.updateConfigFile(cfe);
        }

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

//        profileDao.deleteProfileConfig(asp);
//        batchConfigs(asp, "Y", yamlParis);
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

