package com.javayh.gateway.properties;

import com.javayh.gateway.config.load.GrayEnvLoadBalancer;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author haiji
 */
/*
server:
  gray:
    proVersion: 1
    enable: true
    grayUsers:  abc,ii,ss,kk,bb,pp
    grayVersion:  2


 */
@Data
// 动态刷新
@RefreshScope
@Configuration
@ConfigurationProperties(prefix = "server.gray")
public class ServerGrayProperty {

    /**
     * 生产的版本
     */
    private String proVersion;
    /**
     * 需要灰度的人员列表
     */
    private List<String> grayUsers;

    /**
     * 灰度的版本
     */
    private String grayVersion;

    /**
     * 权重
     */
    private Double weight;

    /**
     * 是否开启{@link GrayEnvLoadBalancer} 的方式进行灰度发布
     */
    private Boolean enable = true;
}
