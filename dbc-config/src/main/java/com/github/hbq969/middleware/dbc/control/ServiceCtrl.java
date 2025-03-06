package com.github.hbq969.middleware.dbc.control;

import com.github.hbq969.code.common.restful.ReturnMessage;
import com.github.hbq969.code.common.spring.context.SpringContext;
import com.github.hbq969.code.common.utils.I18nUtils;
import com.github.hbq969.code.sm.perm.api.SMRequiresPermissions;
import com.github.hbq969.middleware.dbc.dao.entity.ServiceEntity;
import com.github.hbq969.middleware.dbc.service.Service;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("dbc-DbcCtrl")
@Api(tags = "配置中心-服务管理接口")
@RequestMapping(path = "/dbc-ui/service")
@Slf4j
public class ServiceCtrl {

    @Autowired
    private Service service;

    @Autowired
    private SpringContext context;

    @ApiOperation("保存服务")
    @RequestMapping(path = "", method = RequestMethod.POST)
    @ResponseBody
    @SMRequiresPermissions(menu = "Service", apiKey = "saveService", apiDesc = "保存服务")
    public ReturnMessage<?> saveService(@RequestBody ServiceEntity service) {
        this.service.saveService(service);
        return ReturnMessage.success(I18nUtils.getMessage(context, "save.result"));
    }

    @ApiOperation("更新服务")
    @RequestMapping(path = "", method = RequestMethod.PUT)
    @ResponseBody
    @SMRequiresPermissions(menu = "Service", apiKey = "updateService", apiDesc = "更新服务")
    public ReturnMessage<?> updateService(@RequestBody ServiceEntity service) {
        this.service.updateService(service);
        return ReturnMessage.success(I18nUtils.getMessage(context, "update.result"));
    }

    @ApiOperation("删除服务")
    @RequestMapping(path = "", method = RequestMethod.DELETE)
    @ResponseBody
    @SMRequiresPermissions(menu = "Service", apiKey = "deleteService", apiDesc = "删除服务")
    public ReturnMessage<?> deleteService(@RequestBody ServiceEntity entity) {
        this.service.deleteService(entity);
        return ReturnMessage.success(I18nUtils.getMessage(context, "delete.result"));
    }

    @ApiOperation("分页查询服务列表")
    @RequestMapping(path = "/list", method = RequestMethod.POST)
    @ResponseBody
    @SMRequiresPermissions(menu = "Service", apiKey = "queryServicePageList", apiDesc = "分页查询服务列表")
    public ReturnMessage<PageInfo<ServiceEntity>> queryServiceList(
            @RequestParam(name = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(name = "pageSize", defaultValue = "10") int pageSize,
            @RequestBody ServiceEntity service) {
        return ReturnMessage.success(this.service.queryServiceList(service, pageNum, pageSize));
    }
}
