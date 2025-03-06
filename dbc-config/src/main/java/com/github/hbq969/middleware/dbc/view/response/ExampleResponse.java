package com.github.hbq969.middleware.dbc.view.response;

import com.github.hbq969.code.common.utils.FormatTime;
import lombok.Data;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

/**
 * @author : hbq969@gmail.com
 * @description : 示例相应对象
 * @createTime : 2023/8/11 09:52
 */
@Data
public class ExampleResponse {

    private String cacheRefreshTime = FormatTime.YYYYMMDDHHMISS.withMills();

    public static void main(String[] args) throws Exception {
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource resource = resolver.getResource("classpath*:i18n/messages.properties");
        System.out.println(resource);
    }
}
