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
 * 文件名称: BaseClientStarter.java
 * 模块: sandbox-core
 * 包: io.agentscope.runtime.sandbox.manager.client.container
 *
 * 容器客户端启动器抽象基类，负责容器客户端的初始化和配置。
 * 根据容器客户端类型（Docker、AgentBay 等）创建并配置对应的客户端实例。
 * 使用工厂模式，具体实现包括 DockerClientStarter、AgentBayClientStarter 等。
 */

package io.agentscope.runtime.sandbox.manager.client.container;

import io.agentscope.runtime.sandbox.manager.model.container.ContainerClientType;
import io.agentscope.runtime.sandbox.manager.utils.PortManager;

public abstract class BaseClientStarter {
    private final ContainerClientType containerClientType;
    
    public ContainerClientType getContainerClientType() {
        return containerClientType;
    }
    
    public BaseClientStarter(ContainerClientType containerClientType){
        this.containerClientType = containerClientType;
    }

    public abstract BaseClient startClient(PortManager portManager);
}
