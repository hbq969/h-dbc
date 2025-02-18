package com.github.hbq969.middleware.dbc.control;

import com.github.hbq969.code.common.restful.ReturnMessage;
import com.github.hbq969.code.sm.login.session.UserContext;
import com.github.hbq969.middleware.dbc.dao.entity.ConfigEntity;
import com.github.hbq969.middleware.dbc.dao.entity.ConfigFileEntity;
import com.github.hbq969.middleware.dbc.dao.entity.ConfigProfileEntity;
import com.github.hbq969.middleware.dbc.dao.entity.ServiceConfigEntity;
import com.github.hbq969.middleware.dbc.model.AccountServiceProfile;
import com.github.hbq969.middleware.dbc.service.ConfigService;
import com.github.hbq969.middleware.dbc.view.request.ConfigProfileQuery;
import com.github.hbq969.middleware.dbc.view.request.ConfigQuery;
import com.github.hbq969.middleware.dbc.view.request.DeleteConfigMultiple;
import com.github.hbq969.middleware.dbc.view.request.DownFile;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

@RestController("dbc-ConfigCtrl")
@Api(tags = "配置中心-配置管理接口")
@RequestMapping(path = "/dbc-ui/config")
@Slf4j
public class ConfigCtrl {

    @Autowired
    private ConfigService configService;

    @ApiOperation("获取应用配置环境列表")
    @RequestMapping(path = "/profile/list", method = RequestMethod.POST)
    @ResponseBody
    public ReturnMessage<PageInfo<ConfigProfileEntity>> queryConfigProfileList(@RequestParam(name = "pageNum", defaultValue = "1") int pageNum, @RequestParam(name = "pageSize", defaultValue = "10") int pageSize, @RequestBody ConfigProfileQuery q) {
        return ReturnMessage.success(configService.queryConfigProfileList(q, pageNum, pageSize));
    }

    @ApiOperation("保存配置")
    @RequestMapping(path = "", method = RequestMethod.POST)
    @ResponseBody
    public ReturnMessage<?> saveConfig(@RequestBody ConfigQuery cq) {
        configService.saveConfig(cq.getAsp(), cq.getConfig());
        return ReturnMessage.success("保存成功");
    }

    @ApiOperation("更新配置")
    @RequestMapping(path = "", method = RequestMethod.PUT)
    @ResponseBody
    public ReturnMessage<?> updateConfig(@RequestBody ConfigQuery cq) {
        configService.updateConfig(cq.getAsp(), cq.getConfig());
        return ReturnMessage.success("更新成功");
    }

    @ApiOperation("批量更新配置")
    @RequestMapping(path = "/batch", method = RequestMethod.PUT)
    @ResponseBody
    public ReturnMessage<?> batchUpdateConfig(@RequestBody List<ServiceConfigEntity> rows) {
        configService.batchUpdateConfig(rows);
      return ReturnMessage.success("更新成功");
    }

    @ApiOperation("删除配置")
    @RequestMapping(path = "", method = RequestMethod.DELETE)
    @ResponseBody
    public ReturnMessage<?> deleteConfig(@RequestBody ConfigQuery cq) {
        configService.deleteConfig(cq.getAsp(), cq.getConfig());
        return ReturnMessage.success("删除成功");
    }

    @ApiOperation("批量删除配置")
    @RequestMapping(path = "/batch", method = RequestMethod.DELETE)
    @ResponseBody
    public ReturnMessage<?> batchDeleteConfig(@RequestBody List<ServiceConfigEntity> rows) {
        configService.batchDeleteConfig(rows);
      return ReturnMessage.success("删除成功");
    }

    @ApiOperation("批量删除配置")
    @RequestMapping(path = "/multiple", method = RequestMethod.DELETE)
    @ResponseBody
    public ReturnMessage<?> deleteConfigMultiple(@RequestBody DeleteConfigMultiple dcm) {
        configService.deleteConfigMultiple(dcm);
        return ReturnMessage.success("删除成功");
    }

    @ApiOperation("分页查询配置列表")
    @RequestMapping(path = "/list", method = RequestMethod.POST)
    @ResponseBody
    public ReturnMessage<PageInfo<ConfigEntity>> queryConfigList(@RequestParam(name = "pageNum", defaultValue = "1") int pageNum, @RequestParam(name = "pageSize", defaultValue = "10") int pageSize, @RequestBody ConfigQuery cq) {
        return ReturnMessage.success(configService.queryConfigList(cq.getAsp(), cq.getConfig(), pageNum, pageSize));
    }

    @ApiOperation("导入配置")
    @RequestMapping(path = "/import", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public ReturnMessage<?> configImport(@RequestParam("username") String username, @RequestParam("serviceId") String serviceId, @RequestParam("profileName") String profileName, @RequestParam("file") MultipartFile file, @RequestParam("cover") String cover) {
        if (UserContext.permitAllow(username)) {
            AccountServiceProfile asp = new AccountServiceProfile();
            asp.setUsername(username);
            asp.setServiceId(serviceId);
            asp.setProfileName(profileName);
            configService.configImport(asp, file, cover);
            return ReturnMessage.success("导入成功");
        } else {
            throw new UnsupportedOperationException("账号无此操作权限");
        }
    }

    @ApiOperation("查询yml配置文件信息")
    @RequestMapping(path = "/file/info", method = RequestMethod.POST)
    @ResponseBody
    public ReturnMessage<ConfigFileEntity> getConfigFile(@RequestBody AccountServiceProfile asp) {
        return ReturnMessage.success(configService.queryConfigFile(asp));
    }

    @ApiOperation("更新yml配置文件信息")
    @RequestMapping(path = "/file", method = RequestMethod.POST)
    @ResponseBody
    public ReturnMessage<?> updateConfigFile(@RequestBody ConfigFileEntity cfe) {
        configService.updateConfigFile(cfe);
        return ReturnMessage.success("更新成功");
    }

    @ApiOperation("下载配置文件")
    @RequestMapping(path = "/download", method = RequestMethod.POST)
    public void downFile(HttpServletResponse response, @RequestBody DownFile downFile) {
        configService.downFile(response, downFile);
    }

    @ApiOperation("精确查询账号下所有服务的指定配置")
    @RequestMapping(path = "/list/services", method = RequestMethod.POST)
    @ResponseBody
    public ReturnMessage<List<ServiceConfigEntity>> queryAllProfilesThisConfig(@RequestBody Map map) {
        return ReturnMessage.success(configService.queryAllProfilesThisConfig(map));
    }

}
