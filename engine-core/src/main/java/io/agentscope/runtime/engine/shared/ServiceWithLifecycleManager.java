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
 * 文件名称: ServiceWithLifecycleManager.java
 * 模块: engine-core
 * 包: io.agentscope.runtime.engine.shared
 *
 * 异步服务生命周期管理抽象基类。
 * 结合 {@link Service} 接口和 {@link AutoCloseable} 接口，
 * 为服务提供统一的异步生命周期管理框架，支持 try-with-resources 自动资源清理。
 */
package io.agentscope.runtime.engine.shared;

import java.util.concurrent.CompletableFuture;

/**
 * 具有生命周期管理能力的异步服务抽象基类。
 *
 * <p>角色：为大多数异步服务实现提供便利的抽象基类。同时实现 {@link Service} 接口
 * 和 {@link AutoCloseable} 接口，使得服务实例可以使用 try-with-resources 语法
 * 自动完成资源清理。</p>
 *
 * <p>设计模式：模板方法模式（Template Method Pattern）—— {@link #close()} 方法
 * 提供了默认实现（调用 stop().get()），子类只需关注具体的异步启动/停止逻辑。</p>
 */
public abstract class ServiceWithLifecycleManager implements Service, AutoCloseable {

    /**
     * 异步启动服务，子类必须实现。
     */
    @Override
    public abstract CompletableFuture<Void> start();

    /**
     * 异步停止服务，子类必须实现。
     */
    @Override
    public abstract CompletableFuture<Void> stop();

    /**
     * 异步检查服务健康状态，子类必须实现。
     */
    @Override
    public abstract CompletableFuture<Boolean> health();

    /**
     * 关闭服务，委托给 {@link #stop()} 方法。
     * 支持 try-with-resources 语法，确保资源被正确释放。
     *
     * @throws Exception 如果停止过程中发生异常
     */
    @Override
    public void close() throws Exception {
        stop().get();
    }
}
