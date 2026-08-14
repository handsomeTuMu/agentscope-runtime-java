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
 * 文件名称: Service.java
 * 模块: engine-core
 * 包: io.agentscope.runtime.engine.shared
 *
 * 异步服务基础接口，定义所有服务必须实现的生命周期方法。
 * 与 io.agentscope.runtime.engine.services.Service 不同，此版本采用异步设计
 * （基于 CompletableFuture），适用于需要异步初始化/清理的服务。
 */
package io.agentscope.runtime.engine.shared;

import java.util.concurrent.CompletableFuture;

/**
 * 异步服务接口。
 *
 * <p>角色：定义所有引擎服务（如环境服务、会话服务、内存服务等）的统一异步契约。
 * 每个服务都必须实现异步的启动、停止和健康检查方法。</p>
 *
 * <p>设计模式：策略模式 —— 不同的服务实现可以提供各自的行为，
 * 但对外暴露统一基于 CompletableFuture 的异步接口。</p>
 */
public interface Service {

    /**
     * 异步启动服务，初始化必要的资源或连接。
     *
     * @return CompletableFuture&lt;Void&gt; 异步启动结果，完成后表示服务已就绪
     */
    CompletableFuture<Void> start();

    /**
     * 异步停止服务，释放已获取的资源。
     *
     * @return CompletableFuture&lt;Void&gt; 异步停止结果，完成后表示服务已清理
     */
    CompletableFuture<Void> stop();

    /**
     * 异步检查服务的健康状态。
     *
     * @return CompletableFuture&lt;Boolean&gt; 异步健康检查结果，true 表示健康，false 表示不健康
     */
    CompletableFuture<Boolean> health();
}
