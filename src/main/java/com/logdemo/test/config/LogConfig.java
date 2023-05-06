package com.logdemo.test.config;

import com.logdemo.core.annotation.MethodDesc;
import com.logdemo.core.log.LogConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author lsw
 * @Date 2023/5/6 13:44
 */
@Configuration
public class LogConfig {

    @Bean
    public LogConfiguration logConfiguration(){
        LogConfiguration configuration = new LogConfiguration<>();
        configuration.setAnnotationClass(MethodDesc.class);
        return configuration;
    }

}
