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
 * 包: io.agentscope.runtime.engine.services
 *
 * 带生命周期管理的服务抽象基类。
 * 实现了 {@link Service} 接口，为子类提供了统一的抽象方法签名。
 * 子类需要实现具体的 start()、stop() 和 health() 逻辑。
 */

package io.agentscope.runtime.engine.services;

/**
 * 具有生命周期管理能力的服务抽象基类（同步版本）。
 *
 * <p>角色：为大多数服务实现提供便利的抽象基类，统一了 Service 接口的方法签名。
 * 子类只需关注具体的业务逻辑，而无需关心接口定义的细节。</p>
 *
 * <p>设计模式：模板方法模式（Template Method Pattern）—— 基类定义框架，
 * 子类实现具体步骤。</p>
 *
 * <p><b>使用示例：</b>
 * <pre>{@code
 * public class MyService extends ServiceWithLifecycleManager {
 *     @Override
 *     public void start() {
 *         // 初始化资源
 *     }
 *
 *     @Override
 *     public void stop() {
 *         // 清理资源
 *     }
 *
 *     @Override
 *     public boolean health() {
 *         return true; // 返回健康状态
 *     }
 * }
 * }</pre>
 */
public abstract class ServiceWithLifecycleManager implements Service {

    /**
     * 启动服务，初始化必要的资源或连接。子类必须实现。
     */
    @Override
    public abstract void start();

    /**
     * 停止服务，释放已获取的资源。子类必须实现。
     */
    @Override
    public abstract void stop();

    /**
     * 检查服务的健康状态。子类必须实现。
     *
     * @return true 表示服务健康，false 表示服务不可用
     */
    @Override
    public abstract boolean health();
}

