package com.github.hbq969.middleware.dbc.driver.api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

public interface PublicKeyService {
    @RequestMapping(path = "/api/publicKey", method = RequestMethod.GET)
    @ResponseBody
    Map getPublicKey();
}
