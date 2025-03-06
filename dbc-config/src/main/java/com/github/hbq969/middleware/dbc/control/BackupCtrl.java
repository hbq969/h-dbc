package com.github.hbq969.middleware.dbc.control;

import com.github.hbq969.code.common.restful.ReturnMessage;
import com.github.hbq969.code.common.spring.context.SpringContext;
import com.github.hbq969.code.common.utils.I18nUtils;
import com.github.hbq969.code.sm.perm.api.SMRequiresPermissions;
import com.github.hbq969.middleware.dbc.dao.entity.BackupEntity;
import com.github.hbq969.middleware.dbc.dao.entity.ProfileEntity;
import com.github.hbq969.middleware.dbc.model.AccountServiceProfile;
import com.github.hbq969.middleware.dbc.service.BackupService;
import com.github.hbq969.middleware.dbc.view.request.BatchDeleteBackup;
import com.github.hbq969.middleware.dbc.view.request.BatchDeleteRecovery;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("dbc-BackupCtrl")
@Api(tags = "配置中心-备份、恢复管理接口")
@RequestMapping(path = "/dbc-ui/backup")
@Slf4j
public class BackupCtrl {

    @Autowired
    private SpringContext context;

    @Autowired
    @Qualifier("dbc-BackupProxyImpl")
    private BackupService backupService;

    @ApiOperation("分页查询备份列表")
    @RequestMapping(path = "/list", method = RequestMethod.POST)
    @ResponseBody
    @SMRequiresPermissions(menu = "Backup",apiKey = "queryBackupList",apiDesc = "分页查询备份列表")
    public ReturnMessage<PageInfo<BackupEntity>> queryBackupList(
            @RequestParam(name = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(name = "pageSize", defaultValue = "10") int pageSize,
            @RequestBody AccountServiceProfile asp) {
        return ReturnMessage.success(backupService.queryBackupList(asp, pageNum, pageSize));
    }

    @ApiOperation("获取环境列表")
    @RequestMapping(path = "/profile/list", method = RequestMethod.GET)
    @ResponseBody
    @SMRequiresPermissions(menu = "Backup",apiKey = "queryProfileList",apiDesc = "获取环境列表")
    public ReturnMessage<List<ProfileEntity>> queryProfileList() {
        return ReturnMessage.success(backupService.queryProfileList());
    }

    @ApiOperation("恢复配置")
    @RequestMapping(path = "/recovery", method = RequestMethod.POST)
    @ResponseBody
    @SMRequiresPermissions(menu = "Backup",apiKey = "recovery",apiDesc = "恢复配置")
    public ReturnMessage<?> recovery(@RequestBody BackupEntity bk) {
        backupService.recoveryBackup(bk);
        return ReturnMessage.success(I18nUtils.getMessage(context,"BackupCtrl.recovery.result"));
    }

    @ApiOperation("批量恢复配置")
    @RequestMapping(path = "/recovery/batch", method = RequestMethod.POST)
    @ResponseBody
    @SMRequiresPermissions(menu = "Backup",apiKey = "batchRecovery",apiDesc = "批量恢复配置")
    public ReturnMessage<?> batchRecovery(@RequestBody BatchDeleteRecovery bdr) {
        backupService.recoveryBackups(bdr);
        return ReturnMessage.success(I18nUtils.getMessage(context,"BackupCtrl.recovery.result"));
    }

    @ApiOperation("删除备份记录")
    @RequestMapping(path = "", method = RequestMethod.DELETE)
    @ResponseBody
    @SMRequiresPermissions(menu = "Backup",apiKey = "deleteBackup",apiDesc = "删除备份记录")
    public ReturnMessage<?> deleteBackup(@RequestBody BackupEntity bk) {
        backupService.deleteBackup(bk);
        return ReturnMessage.success(I18nUtils.getMessage(context,"delete.result"));
    }

    @ApiOperation("删除备份记录")
    @RequestMapping(path = "/batch", method = RequestMethod.DELETE)
    @ResponseBody
    @SMRequiresPermissions(menu = "Backup",apiKey = "batchDeleteBackup",apiDesc = "批量删除备份记录")
    public ReturnMessage<?> deleteBackups(@RequestBody BatchDeleteBackup bdb) {
        backupService.deleteBackups(bdb);
        return ReturnMessage.success(I18nUtils.getMessage(context,"delete.result"));
    }
}
