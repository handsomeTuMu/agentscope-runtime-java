/*
 * Copyright 2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * 文件名称: ServiceManager.java
 * 模块: engine-core
 * 包: io.agentscope.runtime.engine.shared
 *
 * 服务管理器抽象基类，提供服务的注册、生命周期管理和查询功能。
 * 该类是引擎基础设施的核心，负责管理所有后台服务（如环境服务、会话服务、
 * 内存服务等）的统一启动、停止和健康检查。
 * 实现了 AutoCloseable 接口，支持 try-with-resources 语法进行自动资源清理。
 */
package io.agentscope.runtime.engine.shared;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务管理器抽象基类。
 *
 * <p>角色：引擎基础设施的核心管理组件，负责所有后台服务的注册、实例化、
 * 生命周期编排（启动/停止）和健康状态监控。</p>
 *
 * <p>职责：</p>
 * <ul>
 *   <li>注册服务：支持按类注册（延迟实例化）和按实例注册（预先实例化）</li>
 *   <li>批量启动所有已注册的服务</li>
 *   <li>批量停止所有服务并清理资源</li>
 *   <li>按名称查询已注册的服务实例</li>
 *   <li>执行所有服务的健康检查</li>
 * </ul>
 *
 * <p>设计模式：</p>
 * <ul>
 *   <li>注册表模式（Registry Pattern）—— 集中管理所有服务实例</li>
 *   <li>模板方法模式（Template Method Pattern）—— {@link #registerDefaultServices()} 由子类实现</li>
 * </ul>
 *
 * <p>线程安全说明：serviceInstances 使用 ConcurrentHashMap 保证并发访问安全；
 * services 列表在构造阶段填充，启动后不再修改。</p>
 */
public abstract class ServiceManager implements AutoCloseable {

    /** 日志记录器 */
    private static final Logger logger = LoggerFactory.getLogger(ServiceManager.class);

    /** 延迟实例化的服务注册信息列表（通过 register() 注册） */
    private final List<ServiceRegistration> services = new ArrayList<>();

    /** 已实例化的服务映射表，key=服务名称，value=服务实例 */
    private final Map<String, Service> serviceInstances = new ConcurrentHashMap<>();

    /**
     * 构造函数，初始化并注册默认服务。
     * 子类通过实现 {@link #registerDefaultServices()} 来定义各自需要注册的默认服务集。
     */
    public ServiceManager() {
        registerDefaultServices();
    }

    /**
     * 注册默认服务，子类必须实现此方法以声明各自需要的服务集。
     * 该方法在构造函数中被调用，子类应在其中调用 {@link #register} 或 {@link #registerService}。
     */
    protected abstract void registerDefaultServices();

    /**
     * 注册一个服务类（延迟实例化模式）。
     * 服务将在调用 {@link #start()} 时通过反射创建实例。
     *
     * @param serviceClass 要注册的服务类
     * @param name 可选的服务名称，为 null 时自动使用类名（去除 "Service" 后缀并转小写）
     * @param args 服务初始化的位置参数（预留扩展，当前未使用）
     * @return this，支持链式调用
     * @throws IllegalArgumentException 如果服务名称已被注册
     */
    public ServiceManager register(Class<? extends Service> serviceClass, String name, Object... args) {
        if (name == null) {
            // 自动生成服务名称：类名去掉 "Service" 后缀，转小写
            name = serviceClass.getSimpleName().replace("Service", "").toLowerCase();
        }

        // 检查服务名称是否已被注册
        if (serviceInstances.containsKey(name)) {
            throw new IllegalArgumentException("Service with name '" + name + "' is already registered");
        }

        services.add(new ServiceRegistration(serviceClass, name, args));
        logger.info("Registered service: {}({})", name, serviceClass.getSimpleName());
        return this;
    }

    /**
     * 注册一个已实例化的服务对象。
     *
     * @param name 服务名称
     * @param service 服务实例
     * @return this，支持链式调用
     * @throws IllegalArgumentException 如果服务名称已被注册
     */
    public ServiceManager registerService(String name, Service service) {
        if (serviceInstances.containsKey(name)) {
            throw new IllegalArgumentException("Service with name '" + name + "' is already registered");
        }

        serviceInstances.put(name, service);
        return this;
    }

    /**
     * 异步启动所有已注册的服务。
     *
     * <p>启动流程：</p>
     * <ol>
     *   <li>遍历通过 register() 注册的服务类，反射创建实例并启动</li>
     *   <li>遍历通过 registerService() 注册的预实例化服务，逐一启动</li>
     *   <li>如果任何服务启动失败，执行全量回滚（停止已启动的服务）并抛出异常</li>
     * </ol>
     *
     * @return CompletableFuture&lt;Void&gt; 异步启动结果
     */
    public CompletableFuture<Void> start() {
        return CompletableFuture.runAsync(() -> {
            try {
                // 第一阶段：启动通过 register() 注册的延迟实例化服务
                for (ServiceRegistration registration : services) {
                    try {
                        // 通过反射创建服务实例
                        Service instance = registration.serviceClass.getDeclaredConstructor()
                                .newInstance();
                        instance.start().get(); // 阻塞等待异步启动完成
                        serviceInstances.put(registration.name, instance);
                        logger.info("Successfully started service: {}", registration.name);
                    } catch (Exception e) {
                        logger.error("Failed to start service: {} - {}", registration.name, e.getMessage());
                        throw new RuntimeException("Failed to start service: " + registration.name, e);
                    }
                }

                // 第二阶段：启动通过 registerService() 注册的预实例化服务
                for (Map.Entry<String, Service> entry : serviceInstances.entrySet()) {
                    String name = entry.getKey();
                    Service service = entry.getValue();
                    // 跳过已在第一阶段启动的服务
                    if (!services.stream().anyMatch(reg -> reg.name.equals(name))) {
                        try {
                            service.start().get();
                        } catch (Exception e) {
                            logger.error("Failed to start pre-instantiated service: {} - {}", name, e.getMessage());
                            throw new RuntimeException("Failed to start pre-instantiated service: " + name, e);
                        }
                    }
                }

            } catch (Exception e) {
                logger.error("Failed to start services{}", e.getMessage());
                // 启动失败时，确保已启动的服务被正确清理
                stop().join();
                throw new RuntimeException("Failed to start services", e);
            }
        });
    }

    /**
     * 异步停止所有服务并清理资源。
     *
     * @return CompletableFuture&lt;Void&gt; 异步停止结果
     */
    public CompletableFuture<Void> stop() {
        return CompletableFuture.runAsync(() -> {
            logger.info("Stopping all services");
            for (Service service : serviceInstances.values()) {
                try {
                    service.stop().get();
                } catch (Exception e) {
                    logger.error("Error stopping service{}", e.getMessage());
                }
            }
            serviceInstances.clear();
            logger.info("All services stopped");
        });
    }

    /**
     * AutoCloseable 接口实现，支持 try-with-resources 语法。
     *
     * @throws Exception 如果停止过程中发生异常
     */
    @Override
    public void close() throws Exception {
        stop().get();
    }

    /**
     * 按名称获取服务实例。
     *
     * @param name 服务名称
     * @return 服务实例
     * @throws IllegalArgumentException 如果指定名称的服务不存在
     */
    public Service getService(String name) {
        Service service = serviceInstances.get(name);
        if (service == null) {
            throw new IllegalArgumentException("Service '" + name + "' not found");
        }
        return service;
    }

    /**
     * 按名称获取服务实例，支持默认值。
     *
     * @param name 服务名称
     * @param defaultService 当服务不存在时返回的默认实例
     * @return 服务实例或默认值
     */
    public Service getService(String name, Service defaultService) {
        return serviceInstances.getOrDefault(name, defaultService);
    }

    /**
     * 检查指定名称的服务是否存在。
     *
     * @param name 服务名称
     * @return true 表示存在，false 表示不存在
     */
    public boolean hasService(String name) {
        return serviceInstances.containsKey(name);
    }

    /**
     * 列出所有已注册的服务名称。
     *
     * @return 服务名称列表
     */
    public List<String> listServices() {
        return new ArrayList<>(serviceInstances.keySet());
    }

    /**
     * 获取所有已注册的服务实例。
     *
     * @return 服务名称到服务实例的映射（副本）
     */
    public Map<String, Service> getAllServices() {
        return new HashMap<>(serviceInstances);
    }

    /**
     * 异步检查所有服务的健康状态。
     *
     * @return CompletableFuture&lt;Map&lt;String, Boolean&gt;&gt; 异步健康检查结果，
     *         key=服务名称，value=健康状态（true=健康）
     */
    public CompletableFuture<Map<String, Boolean>> healthCheck() {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Boolean> healthStatus = new HashMap<>();
            for (Map.Entry<String, Service> entry : serviceInstances.entrySet()) {
                String name = entry.getKey();
                Service service = entry.getValue();
                try {
                    healthStatus.put(name, service.health().get());
                } catch (Exception e) {
                    logger.error("Health check failed for service {} - {}", name, e.getMessage());
                    healthStatus.put(name, false);
                }
            }
            return healthStatus;
        });
    }

    /**
     * 服务注册信息内部类，用于存储延迟实例化服务的注册元数据。
     */
    private static class ServiceRegistration {
        /** 服务类 */
        final Class<? extends Service> serviceClass;
        /** 服务名称 */
        final String name;
        /** 构造参数（预留扩展） */
        final Object[] args;

        ServiceRegistration(Class<? extends Service> serviceClass, String name, Object[] args) {
            this.serviceClass = serviceClass;
            this.name = name;
            this.args = args;
        }
    }
}
