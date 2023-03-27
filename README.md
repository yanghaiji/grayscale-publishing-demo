# grayscale-publishing-demo


> grayscale-publishing-demo 是通过nacos 与gateway实现灰度发布的demo
> 本文项目通过两种方式实现了灰度的过程，1.通过header添加version，2.动态加载需要灰度的内容，实现版本以及指定人员的灰度

## 版本依赖

### spring 相关版本

```properties
    <properties>
        <youhuo.version>0.1-SNAPSHOT</youhuo.version>
        <spring.boot.version>2.7.8</spring.boot.version>
        <spring.cloud.version>2021.0.5</spring.cloud.version>
        <spring.cloud.alibaba.version>2021.0.4.0</spring.cloud.alibaba.version>
    </properties>

```

### nacos

Nacos Server 2.2.0

## 动态配置文件示例

env-config.yaml

```properties
server:
  gray:
    proVersion: 1
    enable: true
    grayUsers:  abc,ii,ss,kk,bb,pp
    grayVersion:  2
```

## api-server

本地`IDEA`测试端口冲突时，可以通过如下命令解决

```
-Dserver.port=8092
```

启动后修改nacos内的元数据

```json
{
	"preserved.register.source": "SPRING_CLOUD",
	"version": "2",
	"nacos.weight":10,
	"nacos.healthy":true,
	"name":"gray-api-01
}

```

