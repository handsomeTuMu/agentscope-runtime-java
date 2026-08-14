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
 * 文件名称: SandboxMap.java
 * 模块: sandbox-core
 * 包: io.agentscope.runtime.sandbox.manager.utils
 *
 * 沙箱映射表接口，维护沙箱 ID 到容器模型的映射关系。
 * 支持沙箱实例的注册、查询、移除和批量管理操作。
 * 实现类包括 InMemorySandboxMap（内存版本）和 RedisSandboxMap（Redis 版本）。
 */

package io.agentscope.runtime.sandbox.manager.utils;

import io.agentscope.runtime.sandbox.manager.model.container.ContainerModel;
import io.agentscope.runtime.sandbox.manager.model.container.SandboxKey;

import java.util.Map;

public interface SandboxMap {
    void addSandbox(SandboxKey sandboxKey, ContainerModel containerModel);

    ContainerModel getSandbox(SandboxKey sandboxKey);

    boolean removeSandbox(SandboxKey sandboxKey);

    ContainerModel getSandbox(String containerId);

    void removeSandbox(String containerId);

    Map<String, ContainerModel> getAllSandboxes();

    boolean containSandbox(SandboxKey sandboxKey);

    boolean containSandbox(String containerId);

    long getTTL(String containerId);
    
    long incrementRefCount(String containerId);

    long decrementRefCount(String containerId);

    long getRefCount(String containerId);
}
