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
 * 文件名称: DeployManager.java
 * 模块: engine-core
 * 包: io.agentscope.runtime.engine
 *
 * 部署管理器接口，定义了 Agent 运行器（Runner）的部署与卸载行为。
 * 不同的运行时环境（如 HTTP 服务器、RocketMQ 消费者等）可以提供各自的实现。
 */
package io.agentscope.runtime.engine;


/**
 * 部署管理器接口。
 *
 * <p>角色：定义 Agent 运行器的生命周期入口点，负责将 Runner 部署到具体的运行环境中。
 * 例如，在 HTTP 服务场景下，部署意味着注册 HTTP 端点并启动服务器；
 * 在消息队列场景下，部署意味着开始消费消息。</p>
 *
 * <p>设计模式：策略模式 / 命令模式 —— 将部署行为抽象化，允许不同实现以多态方式替换。</p>
 */
public interface DeployManager {

    /**
     * 部署指定的 Runner，使其开始在运行环境中接收并处理请求。
     *
     * @param runner 要部署的 Agent 运行器实例
     */
    void deploy(Runner runner);

    /**
     * 卸载当前已部署的 Runner，释放相关资源并停止接收新请求。
     */
    void undeploy();
}
