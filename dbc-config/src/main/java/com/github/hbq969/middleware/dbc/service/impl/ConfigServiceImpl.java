package com.github.hbq969.middleware.dbc.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Pair;
import com.github.hbq969.code.common.spring.context.SpringContext;
import com.github.hbq969.code.common.utils.*;
import com.github.hbq969.code.dict.service.api.impl.MapDictHelperImpl;
import com.github.hbq969.middleware.dbc.dao.ConfigDao;
import com.github.hbq969.middleware.dbc.dao.entity.ConfigEntity;
import com.github.hbq969.middleware.dbc.dao.entity.ConfigProfileEntity;
import com.github.hbq969.middleware.dbc.model.AccountServiceProfile;
import com.github.hbq969.middleware.dbc.service.ConfigService;
import com.github.hbq969.middleware.dbc.view.request.ConfigProfileQuery;
import com.github.hbq969.middleware.dbc.view.request.DeleteConfigMultiple;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Service("dbc-ConfigServiceImpl")
@Slf4j
public class ConfigServiceImpl implements ConfigService {

    @Autowired
    private ConfigDao configDao;

    @Autowired
    private SpringContext context;

    @Autowired
    private MapDictHelperImpl dict;

    @Autowired
    private FileReaderFacade fileReader;

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
    }

    @Override
    public void updateConfig(AccountServiceProfile asp, ConfigEntity config) {
        asp.userInitial(context);
        config.setUpdatedAt(FormatTime.nowSecs());
        configDao.updateConfig(asp, config);
    }

    @Override
    public void deleteConfig(AccountServiceProfile asp, ConfigEntity q) {
        asp.userInitial(context);
        configDao.deleteConfig(asp, q);
    }

    @Override
    public void deleteConfigMultiple(DeleteConfigMultiple dcm) {
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
        String sqlSave = "insert into h_dbc_config(app,username,service_id,profile_name,config_key,config_value,created_at) values(?,?,?,?,?,?,?)";
        String sqlUpdate = "update h_dbc_config set config_value=?,updated_at=? where app=? and username=? and service_id=? and profile_name=? and config_key=?";
        DigitSplit.defaultStep(200).split(pairs).forEach(data -> {
            batchSaveConfig(asp, data, sqlSave);
            if (StringUtils.equals("Y", cover)) {
                batchUpdateConfig(asp, data, sqlUpdate);
            }
        });
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

