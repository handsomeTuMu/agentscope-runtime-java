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
 * 文件名称: SandboxTool.java
 * 模块: sandbox-core
 * 包: io.agentscope.runtime.sandbox.tools
 *
 * 沙箱工具抽象基类，扩展自 {@link Tool}，增加了沙箱服务关联、沙箱实例绑定
 * 和工具 Schema 定义。每个具体沙箱工具（如 ReadFileTool、ClickTool 等）都需要
 * 继承此类并实现 getSandboxClass() 和 bind() 抽象方法。
 */
package io.agentscope.runtime.sandbox.tools;


import io.agentscope.runtime.sandbox.box.Sandbox;
import io.agentscope.runtime.sandbox.manager.SandboxService;

import java.util.Map;

/**
 * 沙箱工具抽象基类。
 *
 * <p>角色：为所有需要与沙箱实例交互的工具提供统一基类。
 * 每个工具通过 {@link #bind(Sandbox)} 方法绑定到具体的沙箱实例，
 * 然后通过 {@link SandboxService} 执行实际操作。</p>
 *
 * <p>职责：</p>
 * <ul>
 *   <li>持有沙箱服务引用（SandboxService），用于底层容器操作</li>
 *   <li>持有沙箱实例引用（Sandbox），标识工具操作的上下文</li>
 *   <li>管理工具 Schema（JSON Schema 格式），供 Agent 理解工具的参数规范</li>
 * </ul>
 *
 * <p>设计模式：策略模式 —— 不同的工具实现提供各自的操作策略，
 * 但共享统一的绑定和调用接口。</p>
 */
public abstract class SandboxTool extends Tool {

    /** 沙箱服务，用于底层容器和工具调用操作 */
    protected SandboxService sandboxService;

    /** 绑定的沙箱实例 */
    protected Sandbox sandbox;

    /** 工具的 JSON Schema 定义，描述工具的参数规范 */
    protected Map<String, Object> schema;

    /**
     * 基本构造函数（不含沙箱服务）。
     *
     * @param name 工具名称
     * @param toolType 工具类型
     * @param description 工具描述
     */
    protected SandboxTool(String name, String toolType, String description) {
        super(name, toolType, description);
    }

    /**
     * 带沙箱服务的构造函数。
     *
     * @param name 工具名称
     * @param toolType 工具类型
     * @param description 工具描述
     * @param sandboxService 沙箱服务实例
     */
    protected SandboxTool(String name, String toolType, String description,
                          SandboxService sandboxService) {
        super(name, toolType, description);
        this.sandboxService = sandboxService;
    }

    /** @return 沙箱服务实例 */
    public SandboxService getSandboxService() {
        return sandboxService;
    }

    /** @param sandboxService 沙箱服务实例 */
    public void setSandboxService(SandboxService sandboxService) {
        this.sandboxService = sandboxService;
    }

    /** @return 绑定的沙箱实例 */
    public Sandbox getSandbox() {
        return sandbox;
    }

    /** @param sandbox 要绑定的沙箱实例 */
    public void setSandbox(Sandbox sandbox) {
        this.sandbox = sandbox;
    }

    /** @return 工具的 JSON Schema 定义 */
    public Map<String, Object> getSchema() {
        return schema;
    }

    /** @param schema 工具的 JSON Schema 定义 */
    protected void setSchema(Map<String, Object> schema) {
        this.schema = schema;
    }

    /**
     * 获取此工具适用的沙箱类。
     * 用于工具注册和匹配，确保工具只被绑定到兼容的沙箱类型。
     *
     * @return 沙箱类对象
     */
    public abstract Class<? extends Sandbox> getSandboxClass();

    /**
     * 将此工具绑定到指定的沙箱实例。
     * 返回绑定后的工具实例（通常是 this），支持链式调用。
     *
     * @param sandbox 要绑定的沙箱实例
     * @return 绑定后的工具实例
     */
    public abstract SandboxTool bind(Sandbox sandbox);
}
