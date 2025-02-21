package com.github.hbq969.middleware.dbc.view.request;

import com.github.hbq969.code.common.utils.GsonUtils;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Test {
    public static void main(String[] args) throws Exception {
        HikariDataSource ds =new HikariDataSource();
        ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
        ds.setJdbcUrl("jdbc:mysql://docker.for.mac.host.internal:3306/dbc?useUnicode=true&allowPublicKeyRetrieval=true&characterEncoding=utf-8&useSSL=false&autoReconnect=true&failOverReadOnly=false&maxReconnects=15000");
        ds.setUsername("dbc");
        ds.setPassword("123456");
        JdbcTemplate jt = new JdbcTemplate(ds);
        List<Map<String,Object>> list=jt.queryForList("select pair_key AS \"key\",pair_value AS \"value\" from h_dict_pairs where dict_name='h-dbc,lang,ja-JP'");
        Map<String,String> map = new LinkedHashMap<>();
        for (Map<String, Object> e : list) {
            map.put(e.get("key").toString(),e.get("value").toString());
        }
        System.out.println();
        System.out.println(GsonUtils.toJson(map));
    }
}
