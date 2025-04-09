package com.github.hbq969.middleware.dbc.control;

import com.github.hbq969.code.common.log.api.Log;
import com.github.hbq969.code.common.restful.ReturnMessage;
import com.github.hbq969.code.common.spring.advice.log.LogSet;
import com.github.hbq969.code.common.spring.context.SpringContext;
import com.github.hbq969.code.common.utils.I18nUtils;
import com.github.hbq969.code.dict.service.api.impl.MapDictHelperImpl;
import com.github.hbq969.code.sm.login.session.UserContext;
import com.github.hbq969.code.sm.perm.api.SMRequiresPermissions;
import com.github.hbq969.middleware.dbc.dao.entity.ConfigEntity;
import com.github.hbq969.middleware.dbc.dao.entity.ConfigFileEntity;
import com.github.hbq969.middleware.dbc.dao.entity.ConfigProfileEntity;
import com.github.hbq969.middleware.dbc.dao.entity.ServiceConfigEntity;
import com.github.hbq969.middleware.dbc.model.APIModel;
import com.github.hbq969.middleware.dbc.model.AccountServiceProfile;
import com.github.hbq969.middleware.dbc.service.CacheService;
import com.github.hbq969.middleware.dbc.service.ConfigService;
import com.github.hbq969.middleware.dbc.view.request.*;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController("dbc-ConfigCtrl")
@Api(tags = "配置中心-配置管理接口")
@RequestMapping(path = "/dbc-ui/config")
@Slf4j
public class ConfigCtrl {

    @Autowired
    @Qualifier("dbc-ConfigServiceRBACImpl")
    private ConfigService configService;

    @Autowired
    private SpringContext context;

    @ApiOperation("获取应用配置环境列表")
    @RequestMapping(path = "/profile/list", method = RequestMethod.POST)
    @ResponseBody
    @SMRequiresPermissions(menu = "Service", apiKey = "queryConfigProfileList", apiDesc = "获取应用配置环境列表")
    public ReturnMessage<PageInfo<ConfigProfileEntity>> queryConfigProfileList(@RequestParam(name = "pageNum", defaultValue = "1") int pageNum, @RequestParam(name = "pageSize", defaultValue = "10") int pageSize, @RequestBody ConfigProfileQuery q) {
        return ReturnMessage.success(configService.queryConfigProfileList(q, pageNum, pageSize));
    }

    @ApiOperation("获取页面初始化数据")
    @RequestMapping(path = "/initial", method = RequestMethod.GET)
    @ResponseBody
    public ReturnMessage<?> queryInitial() {
        Map<String, Object> result = new HashMap<>();
        MapDictHelperImpl dict = context.getBean(MapDictHelperImpl.class);
        result.put("dataType", dict == null ? Collections.EMPTY_LIST : dict.queryPairList("config,dataType"));
        return ReturnMessage.success(result);
    }

    @ApiOperation("保存配置")
    @RequestMapping(path = "", method = RequestMethod.POST)
    @ResponseBody
    @SMRequiresPermissions(menu = "Service", apiKey = "saveConfig", apiDesc = "保存配置")
    @Log(collectResult = true)
    public ReturnMessage<?> saveConfig(@RequestBody ConfigQuery cq) {
        configService.saveConfig(cq.getAsp(), cq.getConfig());
        return ReturnMessage.success(I18nUtils.getMessage(context, "save.result"));
    }

    @ApiOperation("更新配置")
    @RequestMapping(path = "", method = RequestMethod.PUT)
    @ResponseBody
    @SMRequiresPermissions(menu = "Service", apiKey = "updateConfig", apiDesc = "更新配置")
    @Log(collectResult = true)
    public ReturnMessage<?> updateConfig(@RequestBody ConfigQuery cq) {
        configService.updateConfig(cq.getAsp(), cq.getConfig());
        return ReturnMessage.success(I18nUtils.getMessage(context, "update.result"));
    }

    @ApiOperation("批量更新配置")
    @RequestMapping(path = "/batch", method = RequestMethod.PUT)
    @ResponseBody
    @SMRequiresPermissions(menu = "Service", apiKey = "batchUpdateConfig", apiDesc = "批量更新配置")
    @Log(collectResult = true)
    public ReturnMessage<?> batchUpdateConfig(@RequestBody List<ServiceConfigEntity> rows) {
        configService.batchUpdateConfig(rows);
        return ReturnMessage.success(I18nUtils.getMessage(context, "update.result"));
    }

    @ApiOperation("删除配置")
    @RequestMapping(path = "", method = RequestMethod.DELETE)
    @ResponseBody
    @SMRequiresPermissions(menu = "Service", apiKey = "deleteConfig", apiDesc = "删除配置")
    @Log(collectResult = true)
    public ReturnMessage<?> deleteConfig(@RequestBody ConfigQuery cq) {
        configService.deleteConfig(cq.getAsp(), cq.getConfig());
        return ReturnMessage.success(I18nUtils.getMessage(context, "delete.result"));
    }

    @ApiOperation("批量删除配置")
    @RequestMapping(path = "/batch", method = RequestMethod.DELETE)
    @ResponseBody
    @SMRequiresPermissions(menu = "Service", apiKey = "batchDeleteConfig", apiDesc = "批量删除配置")
    @Log(collectResult = true)
    public ReturnMessage<?> batchDeleteConfig(@RequestBody List<ServiceConfigEntity> rows) {
        configService.batchDeleteConfig(rows);
        return ReturnMessage.success(I18nUtils.getMessage(context, "delete.result"));
    }

    @ApiOperation("批量删除配置")
    @RequestMapping(path = "/multiple", method = RequestMethod.DELETE)
    @ResponseBody
    @SMRequiresPermissions(menu = "Service", apiKey = "deleteConfigMultiple", apiDesc = "批量删除配置")
    @Log(collectResult = true)
    public ReturnMessage<?> deleteConfigMultiple(@RequestBody DeleteConfigMultiple dcm) {
        configService.deleteConfigMultiple(dcm);
        return ReturnMessage.success(I18nUtils.getMessage(context, "delete.result"));
    }

    @ApiOperation("分页查询配置列表")
    @RequestMapping(path = "/list", method = RequestMethod.POST)
    @ResponseBody
    @SMRequiresPermissions(menu = "Service", apiKey = "queryConfigPageList", apiDesc = "分页查询配置列表")
    public ReturnMessage<PageInfo<ConfigEntity>> queryConfigList(@RequestParam(name = "pageNum", defaultValue = "1") int pageNum, @RequestParam(name = "pageSize", defaultValue = "10") int pageSize, @RequestBody ConfigQuery cq) {
        return ReturnMessage.success(configService.queryConfigList(cq.getAsp(), cq.getConfig(), pageNum, pageSize));
    }

    @ApiOperation("导入配置")
    @RequestMapping(path = "/import", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    @SMRequiresPermissions(menu = "Service", apiKey = "configImport", apiDesc = "导入配置")
    @Log(collectResult = true)
    public ReturnMessage<?> configImport(
            @RequestParam("username") String username,
            @RequestParam("serviceId") String serviceId,
            @RequestParam("profileName") String profileName,
            @RequestParam("file") MultipartFile file,
            @RequestParam("cover") String cover,
            @RequestParam("backup") String backup) {
        if (UserContext.permitAllow(username)) {
            AccountServiceProfile asp = new AccountServiceProfile();
            asp.setUsername(username);
            asp.setServiceId(serviceId);
            asp.setProfileName(profileName);
            configService.configImport(asp, file, cover, backup);
            return ReturnMessage.success(I18nUtils.getMessage(context, "import.result"));
        } else {
            throw new UnsupportedOperationException(I18nUtils.getMessage(context, "BackupServiceImpl.msg1"));
        }
    }

    @ApiOperation("查询yml配置文件信息")
    @RequestMapping(path = "/file/info", method = RequestMethod.POST)
    @ResponseBody
    @SMRequiresPermissions(menu = "Service", apiKey = "getConfigFile", apiDesc = "查询yml配置文件信息")
    public ReturnMessage<ConfigFileEntity> getConfigFile(@RequestBody AccountServiceProfile asp) {
        return ReturnMessage.success(configService.queryConfigFile(asp));
    }

    @ApiOperation("查询当前配置和待恢复配置信息")
    @RequestMapping(path = "/file/info/currentAndBackup", method = RequestMethod.POST)
    @ResponseBody
    @SMRequiresPermissions(menu = "Backup", apiKey = "getCurrentAndBackupFile", apiDesc = "查询当前配置和待恢复配置信息")
    public ReturnMessage<ConfigFileEntity> getCurrentAndBackupFile(@RequestBody CompareCurrentAndBackupFile ccabf) {
        return ReturnMessage.success(configService.getCurrentAndBackupFile(ccabf));
    }

    @ApiOperation("更新yml配置文件信息")
    @RequestMapping(path = "/file", method = RequestMethod.POST)
    @ResponseBody
    @SMRequiresPermissions(menu = "Service", apiKey = "updateConfigFile", apiDesc = "更新yml配置文件信息")
    @LogSet(printIn = false)
    @Log(collectResult = true)
    public ReturnMessage<?> updateConfigFile(@RequestBody ConfigFileEntity cfe) {
        configService.updateConfigFile(cfe);
        return ReturnMessage.success(I18nUtils.getMessage(context, "update.result"));
    }

    @ApiOperation("下载配置文件")
    @RequestMapping(path = "/download", method = RequestMethod.POST)
    @SMRequiresPermissions(menu = "Service", apiKey = "downFile", apiDesc = "下载配置文件")
    public void downFile(HttpServletResponse response, @RequestBody DownFile downFile) {
        configService.downFile(response, downFile);
    }

    @ApiOperation("下载服务集成文件")
    @RequestMapping(path = "/bootstrap/download", method = RequestMethod.POST)
    @SMRequiresPermissions(menu = "Service", apiKey = "downBootstrapFile", apiDesc = "下载服务集成文件")
    public void downBootstrapFile(HttpServletResponse response, @RequestBody Map map) {
        configService.downBootstrapFile(response, map);
    }

    @ApiOperation("精确查询账号下所有服务的指定配置")
    @RequestMapping(path = "/list/services", method = RequestMethod.POST)
    @ResponseBody
    @SMRequiresPermissions(menu = "Service", apiKey = "queryAllProfilesThisConfig", apiDesc = "精确查询账号下所有服务的指定配置")
    public ReturnMessage<List<ServiceConfigEntity>> queryAllProfilesThisConfig(@RequestBody Map map) {
        return ReturnMessage.success(configService.queryAllProfilesThisConfig(map));
    }

    @ApiOperation("备份配置")
    @RequestMapping(path = "/backup", method = RequestMethod.POST)
    @ResponseBody
    @SMRequiresPermissions(menu = "Service", apiKey = "backup", apiDesc = "备份配置")
    @Log(collectResult = true)
    public ReturnMessage<?> backup(@RequestBody AccountServiceProfile asp) {
        configService.backup(asp);
        return ReturnMessage.success(I18nUtils.getMessage(context, "ConfigCtrl.backup.result"));
    }

}
