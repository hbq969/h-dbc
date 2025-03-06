package com.github.hbq969.middleware.dbc.control;

import com.github.hbq969.code.common.restful.ReturnMessage;
import com.github.hbq969.code.common.spring.context.SpringContext;
import com.github.hbq969.code.common.utils.I18nUtils;
import com.github.hbq969.code.sm.perm.api.SMRequiresPermissions;
import com.github.hbq969.middleware.dbc.dao.entity.ProfileEntity;
import com.github.hbq969.middleware.dbc.model.AccountService;
import com.github.hbq969.middleware.dbc.model.AccountServiceProfile;
import com.github.hbq969.middleware.dbc.service.ProfileService;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("dbc-ProfileCtrl")
@Api(tags = "配置中心-环境管理接口")
@RequestMapping(path = "/dbc-ui/profile")
@Slf4j
public class ProfileCtrl {
    @Autowired
    private ProfileService profileService;

    @Autowired
    private SpringContext context;

    @ApiOperation("保存环境")
    @RequestMapping(path = "", method = RequestMethod.POST)
    @ResponseBody
    @SMRequiresPermissions(menu = "Profile", apiKey = "saveProfile", apiDesc = "保存环境")
    public ReturnMessage<?> saveProfile(@RequestBody ProfileEntity profile) {
        profileService.saveProfile(profile);
        return ReturnMessage.success(I18nUtils.getMessage(context, "save.result"));
    }

    @ApiOperation("更新环境")
    @RequestMapping(path = "", method = RequestMethod.PUT)
    @ResponseBody
    @SMRequiresPermissions(menu = "Profile", apiKey = "updateProfile", apiDesc = "更新环境")
    public ReturnMessage<?> updateProfile(@RequestBody ProfileEntity profile) {
        profileService.updateProfile(profile);
        return ReturnMessage.success(I18nUtils.getMessage(context, "update.result"));
    }

    @ApiOperation("删除环境")
    @RequestMapping(path = "", method = RequestMethod.DELETE)
    @ResponseBody
    @SMRequiresPermissions(menu = "Profile", apiKey = "deleteProfile", apiDesc = "删除环境")
    public ReturnMessage<?> deleteProfile(ProfileEntity profile) {
        profileService.deleteProfile(profile);
        return ReturnMessage.success(I18nUtils.getMessage(context, "delete.result"));
    }

    @ApiOperation("分页查询环境列表")
    @RequestMapping(path = "/list", method = RequestMethod.POST)
    @ResponseBody
    @SMRequiresPermissions(menu = "Profile", apiKey = "queryProfilePageList", apiDesc = "分页查询环境列表")
    public ReturnMessage<PageInfo<ProfileEntity>> queryProfileList(
            @RequestParam(name = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(name = "pageSize", defaultValue = "10") int pageSize,
            @RequestBody ProfileEntity profile) {
        return ReturnMessage.success(profileService.queryProfileList(profile, pageNum, pageSize));
    }

    @ApiOperation("查询环境列表")
    @RequestMapping(path = "/all", method = RequestMethod.POST)
    @ResponseBody
    @SMRequiresPermissions(menu = "Service", apiKey = "queryProfileList", apiDesc = "查询环境列表")
    public ReturnMessage<PageInfo<ProfileEntity>> queryProfileList(@RequestBody AccountService as) {
        return ReturnMessage.success(profileService.queryProfileList(as));
    }

    @ApiOperation("清空环境配置")
    @RequestMapping(path = "/config", method = RequestMethod.DELETE)
    @ResponseBody
    @SMRequiresPermissions(menu = "Service", apiKey = "clearProfileConfig", apiDesc = "清空环境配置")
    public ReturnMessage<?> clearProfileConfig(@RequestBody AccountServiceProfile asp) {
        profileService.deleteProfileConfig(asp);
        return ReturnMessage.success(I18nUtils.getMessage(context, "clear.result"));
    }

    @ApiOperation("备份配置")
    @RequestMapping(path = "/backup", method = RequestMethod.POST)
    @ResponseBody
    @SMRequiresPermissions(menu = "Profile", apiKey = "backup", apiDesc = "备份配置")
    public ReturnMessage<?> backup(@RequestBody ProfileEntity profile) {
        profileService.backup(profile);
        return ReturnMessage.success(I18nUtils.getMessage(context, "ConfigCtrl.backup.result"));
    }

    @ApiOperation("备份所有配置")
    @RequestMapping(path = "/backup/all", method = RequestMethod.POST)
    @ResponseBody
    @SMRequiresPermissions(menu = "Profile", apiKey = "backupAll", apiDesc = "备份所有配置")
    public ReturnMessage<?> backupAll() {
        profileService.backupAll();
        return ReturnMessage.success(I18nUtils.getMessage(context, "ConfigCtrl.backup.result"));
    }
}
