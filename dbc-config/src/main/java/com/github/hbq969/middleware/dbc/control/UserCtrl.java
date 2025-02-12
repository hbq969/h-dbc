package com.github.hbq969.middleware.dbc.control;


import com.github.hbq969.code.common.restful.ReturnMessage;
import com.github.hbq969.code.sm.login.model.UserInfo;
import com.github.hbq969.code.sm.login.session.UserContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController("dbc-UserCtrl")
@Api(tags = "配置中心-用户信息接口")
@RequestMapping(path = "/dbc-ui/user")
@Slf4j
public class UserCtrl {
    @ApiOperation("获取用户信息")
    @RequestMapping(path = "", method = RequestMethod.GET)
    @ResponseBody
    public ReturnMessage<UserInfo> getUserInfo() {
        UserInfo ui = new UserInfo();
        ui.setUserName(UserContext.get().getUserName());
        ui.setRoleName(UserContext.get().getRoleName());
      return ReturnMessage.success(ui);
    }
}
