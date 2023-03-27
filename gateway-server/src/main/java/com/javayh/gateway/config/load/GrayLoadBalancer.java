package com.javayh.gateway.config.load;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.nacos.balancer.NacosBalancer;
import com.javayh.gateway.util.EnvUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.*;
import org.springframework.cloud.loadbalancer.core.NoopServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 灰度 {@link GrayLoadBalancer} 实现类
 * <p>
 * 根据请求的 header[version] 匹配，筛选满足 metadata[version] 相等的服务实例列表，然后随机 + 权重进行选择一个
 * 1. 假如请求的 header[version] 为空，则不进行筛选，所有服务实例都进行选择
 * 2. 如果 metadata[version] 都不相等，则不进行筛选，所有服务实例都进行选择
 * <p>
 * 注意，考虑到实现的简易，它的权重是使用 Nacos 的 nacos.weight，所以随机 + 权重也是基于 {@link NacosBalancer} 筛选。
 * 也就是说，如果你不使用 Nacos 作为注册中心，需要微调一下筛选的实现逻辑
 *
 * @author haiji
 */
@RequiredArgsConstructor
@Slf4j
public class GrayLoadBalancer implements ReactorServiceInstanceLoadBalancer {

    private static final String VERSION = "version";

    /**
     * 用于获取 serviceId 对应的服务实例的列表
     */
    private final ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;
    /**
     * 需要获取的服务实例名
     * <p>
     * 暂时用于打印 logger 日志
     */
    private final String serviceId;

    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        // 获得 HttpHeaders 属性，实现从 header 中获取 version
        HttpHeaders headers = ((RequestDataContext) request.getContext()).getClientRequest().getHeaders();
        // 选择实例
        ServiceInstanceListSupplier supplier = serviceInstanceListSupplierProvider.getIfAvailable(NoopServiceInstanceListSupplier::new);
        return supplier.get(request).next().map(list -> getInstanceResponse(list, headers));
    }

    private Response<ServiceInstance> getInstanceResponse(List<ServiceInstance> instances, HttpHeaders headers) {
        // 如果服务实例为空，则直接返回
        if (CollUtil.isEmpty(instances)) {
            log.warn("[getInstanceResponse][serviceId({}) 服务实例列表为空]", serviceId);
            return new EmptyResponse();
        }
        // 筛选满足 version 条件的实例列表
        String version = headers.getFirst(VERSION);
        List<ServiceInstance> chooseInstances;
        if (StrUtil.isEmpty(version)) {
            chooseInstances = instances;
        } else {
            chooseInstances = filterList(instances, instance -> version.equals(instance.getMetadata().get("version")));
            if (CollUtil.isEmpty(chooseInstances)) {
                log.warn("[getInstanceResponse][serviceId({}) 没有满足版本({})的服务实例列表，直接使用所有服务实例列表]", serviceId, version);
                chooseInstances = instances;
            }
        }

        // 基于 tag 过滤实例列表
        chooseInstances = filterTagServiceInstances(chooseInstances, headers);

        // 随机 + 权重获取实例列表
        return new DefaultResponse(NacosBalancer.getHostByRandomWeight3(chooseInstances));
    }


    /**
     * 基于 tag 请求头，过滤匹配 tag 的服务实例列表
     * <p>
     * copy from EnvLoadBalancerClient
     *
     * @param instances 服务实例列表
     * @param headers   请求头
     * @return 服务实例列表
     */
    private List<ServiceInstance> filterTagServiceInstances(List<ServiceInstance> instances, HttpHeaders headers) {
        // 情况一，没有 tag 时，直接返回
        String tag = EnvUtils.getTag(headers);
        if (StrUtil.isEmpty(tag)) {
            return instances;
        }

        // 情况二，有 tag 时，使用 tag 匹配服务实例
        List<ServiceInstance> chooseInstances = filterList(instances, instance -> tag.equals(EnvUtils.getTag(instance)));
        if (CollUtil.isEmpty(chooseInstances)) {
            log.warn("[filterTagServiceInstances][serviceId({}) 没有满足 tag({}) 的服务实例列表，直接使用所有服务实例列表]", serviceId, tag);
            chooseInstances = instances;
        }
        return chooseInstances;
    }

    public static <T> List<T> filterList(Collection<T> from, Predicate<T> predicate) {
        if (CollUtil.isEmpty(from)) {
            return new ArrayList<>();
        }
        return from.stream().filter(predicate).collect(Collectors.toList());
    }


}
