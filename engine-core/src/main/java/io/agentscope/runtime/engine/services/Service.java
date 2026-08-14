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
 * 包: io.agentscope.runtime.engine.services
 *
 * 服务基础接口，定义所有引擎服务必须实现的生命周期方法（启动、停止、健康检查）。
 * 该接口采用同步设计，适用于不需要异步操作的场景。
 */

package io.agentscope.runtime.engine.services;

/**
 * 服务接口（同步版本）。
 *
 * <p>角色：定义所有引擎服务（如状态服务、内存服务、会话历史服务等）的统一契约。
 * 每个服务都必须实现启动、停止和健康检查三个生命周期方法。</p>
 *
 * <p>设计模式：策略模式 —— 不同的服务实现提供各自的行为，但对外暴露统一的接口。</p>
 *
 * <p><b>使用示例：</b>
 * <pre>{@code
 * Service service = new MyService();
 *
 * // 手动生命周期管理
 * service.start();
 * try {
 *     // 使用服务
 *     boolean healthy = service.health();
 * } finally {
 *     service.stop();
 * }
 * }</pre>
 */
public interface Service {

    /**
     * 启动服务，初始化必要的资源或连接。
     */
    void start();

    /**
     * 停止服务，释放已获取的资源。
     */
    void stop();

    /**
     * 检查服务的健康状态。
     *
     * @return true 表示服务健康，false 表示服务不可用
     */
    boolean health();
}

