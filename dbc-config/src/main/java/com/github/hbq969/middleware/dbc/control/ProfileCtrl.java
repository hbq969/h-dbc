package com.github.hbq969.middleware.dbc.control;

import com.github.hbq969.code.common.restful.ReturnMessage;
import com.github.hbq969.middleware.dbc.dao.entity.ProfileEntity;
import com.github.hbq969.middleware.dbc.model.AccountService;
import com.github.hbq969.middleware.dbc.model.AccountServiceProfile;
import com.github.hbq969.middleware.dbc.service.ProfileService;
import com.github.hbq969.middleware.dbc.view.request.ConfigProfileQuery;
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

    @ApiOperation("保存环境")
    @RequestMapping(path = "", method = RequestMethod.POST)
    @ResponseBody
    public ReturnMessage<?> saveProfile(@RequestBody ProfileEntity profile) {
        profileService.saveProfile(profile);
        return ReturnMessage.success("保存成功");
    }

    @ApiOperation("更新环境")
    @RequestMapping(path = "", method = RequestMethod.PUT)
    @ResponseBody
    public ReturnMessage<?> updateProfile(@RequestBody ProfileEntity profile) {
        profileService.updateProfile(profile);
        return ReturnMessage.success("更新成功");
    }

    @ApiOperation("删除环境")
    @RequestMapping(path = "", method = RequestMethod.DELETE)
    @ResponseBody
    public ReturnMessage<?> deleteProfile(ProfileEntity profile) {
        profileService.deleteProfile(profile);
        return ReturnMessage.success("删除成功");
    }

    @ApiOperation("分页查询环境列表")
    @RequestMapping(path = "/list", method = RequestMethod.POST)
    @ResponseBody
    public ReturnMessage<PageInfo<ProfileEntity>> queryProfileList(
            @RequestParam(name = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(name = "pageSize", defaultValue = "10") int pageSize,
            @RequestBody ProfileEntity profile) {
        return ReturnMessage.success(profileService.queryProfileList(profile, pageNum, pageSize));
    }

    @ApiOperation("查询环境列表")
    @RequestMapping(path = "/all", method = RequestMethod.POST)
    @ResponseBody
    public ReturnMessage<PageInfo<ProfileEntity>> queryProfileList(@RequestBody AccountService as) {
        return ReturnMessage.success(profileService.queryProfileList(as));
    }

    @ApiOperation("清空环境配置")
    @RequestMapping(path = "/config", method = RequestMethod.DELETE)
    @ResponseBody
    public ReturnMessage<?> clearProfileConfig(@RequestBody AccountServiceProfile asp) {
        profileService.deleteProfileConfig(asp);
        return ReturnMessage.success("清理成功");
    }
}
