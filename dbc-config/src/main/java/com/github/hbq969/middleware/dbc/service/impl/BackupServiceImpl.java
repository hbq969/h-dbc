package com.github.hbq969.middleware.dbc.service.impl;

import cn.hutool.core.lang.UUID;
import com.github.hbq969.code.common.spring.context.SpringContext;
import com.github.hbq969.code.common.utils.DigitSplit;
import com.github.hbq969.code.common.utils.FormatTime;
import com.github.hbq969.code.common.utils.GsonUtils;
import com.github.hbq969.middleware.dbc.dao.BackupDao;
import com.github.hbq969.middleware.dbc.dao.ProfileDao;
import com.github.hbq969.middleware.dbc.dao.ServiceDao;
import com.github.hbq969.middleware.dbc.dao.entity.*;
import com.github.hbq969.middleware.dbc.model.AccountServiceProfile;
import com.github.hbq969.middleware.dbc.service.BackupService;
import com.github.hbq969.middleware.dbc.view.request.BatchDeleteBackup;
import com.github.hbq969.middleware.dbc.view.request.BatchDeleteRecovery;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service("dbc-BackupServiceImpl")
@Slf4j
public class BackupServiceImpl implements BackupService {

    @Autowired
    private BackupDao backupDao;
    @Autowired
    private SpringContext context;
    @Autowired
    private ProfileDao profileDao;
    @Qualifier("dbc-ServiceDao")
    @Autowired
    private ServiceDao serviceDao;

    @Override
    public void backupOnDeleteProfile(ProfileEntity profile) {
        List<ServiceConfigEntity> list = backupDao.queryConfigListByProfileName(profile.getProfileName());
        backup(list);
    }

    private void backup(List<ServiceConfigEntity> list) {
        Map<AccountServiceProfile, List<ServiceConfigEntity>> group = list.stream().collect(Collectors.groupingBy(c -> {
            AccountServiceProfile asp = new AccountServiceProfile();
            asp.setApp(c.getApp());
            asp.setUsername(c.getUsername());
            asp.setServiceId(c.getServiceId());
            asp.setServiceName(c.getServiceName());
            asp.setServiceDesc(c.getServiceDesc());
            asp.setProfileName(c.getProfileName());
            asp.setProfileDesc(c.getProfileDesc());
            return asp;
        }));
        String sql = "insert into h_dbc_config_bk(id,app,username,service_id,service_name,service_desc,profile_name,profile_desc,file_name,backup_content,created_at) values(?,?,?,?,?,?,?,?,?,?,?)";
        for (Map.Entry<AccountServiceProfile, List<ServiceConfigEntity>> entry : group.entrySet()) {
            context.getBean(JdbcTemplate.class).update(sql, ps -> {
                ps.setString(1, UUID.fastUUID().toString(true));
                ps.setString(2, entry.getKey().getApp());
                ps.setString(3, entry.getKey().getUsername());
                ps.setString(4, entry.getKey().getServiceId());
                ps.setString(5, entry.getKey().getServiceName());
                ps.setString(6, entry.getKey().getServiceDesc());
                ps.setString(7, entry.getKey().getProfileName());
                ps.setString(8, entry.getKey().getProfileDesc());
                ps.setString(9, entry.getKey().getFilename());
                ps.setString(10, GsonUtils.toJson(entry.getValue()));
                ps.setLong(11, FormatTime.nowSecs());
            });
        }
    }

    @Override
    public void backupOnDeleteService(ServiceEntity service) {
        List<ServiceConfigEntity> list = backupDao.queryConfigListByServiceId(service.getServiceId());
        backup(list);
    }

    @Override
    public void backupOnClearProfileConfig(AccountServiceProfile asp) {
        List<ServiceConfigEntity> list = backupDao.queryConfigListByServiceAndProfile(asp);
        backup(list);
    }

    @Override
    public void backupOnConfigImport(AccountServiceProfile asp) {
        List<ServiceConfigEntity> list = backupDao.queryConfigListByServiceAndProfile(asp);
        backup(list);
    }

    @Override
    public void backupOnUpdateConfigFile(ConfigFileEntity file) {
        AccountServiceProfile asp = new AccountServiceProfile().propertySet(file);
        List<ServiceConfigEntity> list = backupDao.queryConfigListByServiceAndProfile(asp);
        backup(list);
    }

    @Override
    public PageInfo<BackupEntity> queryBackupList(AccountServiceProfile asp, int pageNum, int pageSize) {
        PageInfo<BackupEntity> pg = PageHelper.startPage(pageNum, pageSize)
                .doSelectPageInfo(() -> backupDao.queryBackupList(asp));
        pg.getList().forEach(e -> e.convertDict(context));
        return pg;
    }

    @Override
    public void deleteBackup(BackupEntity bk) {
        backupDao.deleteBackup(bk);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteBackups(BatchDeleteBackup bdb) {
        bdb.check(context);
        String sql = "delete from h_dbc_config_bk where id=?";
        log.info("批量删除备份数据, {}, {}", sql, bdb.getBackups());
        context.getBean(JdbcTemplate.class).batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                BackupEntity bk = bdb.getBackups().get(i);
                ps.setString(1, bk.getId());
            }

            @Override
            public int getBatchSize() {
                return bdb.getBackups().size();
            }
        });
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void recoveryBackup(BackupEntity bk) {
        BackupEntity entity = backupDao.queryBackup(bk);
        String sql = "select service_id AS \"serviceId\",service_name AS \"serviceName\" from h_dbc_service where service_name=?";
        List<ServiceEntity> services = null;
        try {
            services = context.getBean(JdbcTemplate.class).query(sql, new Object[]{entity.getServiceName()}, new int[]{Types.VARCHAR}, (rs, rowNum) -> {
                ServiceEntity se = new ServiceEntity();
                se.setServiceId(rs.getString(1));
                se.setServiceName(rs.getString(2));
                return se;
            });
        } catch (DataAccessException e) {
        }
        log.info("查询服务是否存在, {}, {}, {}", sql, entity.getServiceName(), services);
        // 服务不存在需要恢复
        long now = FormatTime.nowSecs();
        final String newServiceId = UUID.fastUUID().toString(true);
        if (CollectionUtils.isEmpty(services)) {
            context.getBean(JdbcTemplate.class).update("insert into h_dbc_service(service_id,service_name,service_desc,created_at) values(?,?,?,?)", ps -> {
                ps.setString(1, newServiceId);
                ps.setString(2, entity.getServiceName());
                ps.setString(3, entity.getServiceDesc());
                ps.setLong(4, now);
            });

            context.getBean(JdbcTemplate.class).update("insert into h_dbc_acc_service(app,username,service_id) values(?,?,?)", ps -> {
                ps.setString(1, entity.getApp());
                ps.setString(2, entity.getUsername());
                ps.setString(3, newServiceId);
            });
            log.info("服务{}, {} 创建成功", newServiceId, entity.getServiceName());
        }

        sql = "select count(1) from h_dbc_profiles where profile_name=?";
        int c = context.getBean(JdbcTemplate.class).queryForObject(sql, new Object[]{entity.getProfileName()}, new int[]{Types.VARCHAR}, Integer.class);
        log.info("查询环境是否存在, {}, {}, {}", sql, entity.getProfileName(), c);
        // 环境不存在需要恢复
        if (c == 0) {
            context.getBean(JdbcTemplate.class).update("insert into h_dbc_profiles(profile_name,profile_desc,created_at) values(?,?,?)", ps -> {
                ps.setString(1, entity.getProfileName());
                ps.setString(2, entity.getProfileDesc());
                ps.setLong(3, now);
            });
            context.getBean(JdbcTemplate.class).update("insert into h_dbc_acc_profiles(app,username,profile_name) values(?,?,?)", ps -> {
                ps.setString(1, entity.getApp());
                ps.setString(2, entity.getUsername());
                ps.setString(3, entity.getProfileName());
            });
            log.info("环境 {} 创建成功", entity.getProfileName());
        }
        // 恢复配置
        sql = "delete from h_dbc_config where app=? and username=? and service_id=? and profile_name=?";
        log.info("删除原有配置, {}, [{},{},{}]", sql, entity.getUsername(), entity.getServiceId(), entity.getProfileName());
        context.getBean(JdbcTemplate.class).update(sql, ps -> {
            ps.setString(1, entity.getApp());
            ps.setString(2, entity.getUsername());
            ps.setString(3, entity.getServiceId());
            ps.setString(4, entity.getProfileName());
        });
        if (CollectionUtils.isNotEmpty(services)) {
            ServiceEntity service = services.get(0);
            log.info("删除原有配置, {}, [{},{},{}]", sql, entity.getUsername(), service.getServiceId(), entity.getProfileName());
            context.getBean(JdbcTemplate.class).update(sql, ps -> {
                ps.setString(1, entity.getApp());
                ps.setString(2, entity.getUsername());
                ps.setString(3, service.getServiceId());
                ps.setString(4, entity.getProfileName());
            });
        }
        List<ConfigEntity> configs = GsonUtils.parseArray(entity.getBackupContent(), new TypeToken<List<ConfigEntity>>() {
        });
        String serviceId = newServiceId;
        if (CollectionUtils.isNotEmpty(services)) {
            serviceId = services.get(0).getServiceId();
        }
        final String sid = serviceId;
        DigitSplit.defaultStep(200).split(configs).forEach(sub -> {
            log.info("批量恢复配置, [{},{},{}], {} 个", entity.getUsername(), sid, entity.getProfileName(), sub.getList().size());
            context.getBean(JdbcTemplate.class).batchUpdate("insert into h_dbc_config(app,username,service_id,profile_name,config_key,config_value,data_type,created_at) values(?,?,?,?,?,?,?,?)",
                    new BatchPreparedStatementSetter() {
                        @Override
                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            ConfigEntity ce = sub.getList().get(i);
                            ps.setString(1, entity.getApp());
                            ps.setString(2, entity.getUsername());
                            ps.setString(3, sid);
                            ps.setString(4, entity.getProfileName());
                            ps.setString(5, ce.getConfigKey());
                            ps.setString(6, ce.getConfigValue());
                            ps.setString(7, ce.getDataType());
                            ps.setLong(8, now);
                        }

                        @Override
                        public int getBatchSize() {
                            return sub.getList().size();
                        }
                    });
        });
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void recoveryBackups(BatchDeleteRecovery bdr) {
        bdr.check(context);
        for (BackupEntity bk : bdr.getRecoveries()) {
            recoveryBackup(bk);
        }
    }

    @Override
    public List<ProfileEntity> queryProfileList() {
        return profileDao.queryAllProfileList();
    }
}
