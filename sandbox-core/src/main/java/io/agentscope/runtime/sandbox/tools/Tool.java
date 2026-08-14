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
 * 文件名称: Tool.java
 * 模块: sandbox-core
 * 包: io.agentscope.runtime.sandbox.tools
 *
 * 工具抽象基类，定义沙箱工具的基本属性（名称、类型、描述）。
 * 所有沙箱工具（如文件操作工具、浏览器工具、代码执行工具等）都继承自此类。
 */
package io.agentscope.runtime.sandbox.tools;

/**
 * 工具抽象基类。
 *
 * <p>角色：为所有沙箱工具提供统一的基础属性和接口。
 * 工具是 Agent 在沙箱中执行具体操作（如读写文件、点击网页、执行代码）的抽象单元。</p>
 *
 * <p>设计模式：模板基类模式 —— 定义工具的公共结构，子类实现具体的工具行为。</p>
 */
public abstract class Tool {

    /** 工具名称，唯一标识一个工具 */
    protected String name;

    /** 工具类型（如 "filesystem"、"browser"、"code" 等） */
    protected String toolType;

    /** 工具描述，用于 Agent 理解工具的用途 */
    protected String description;

    /**
     * 构造函数。
     *
     * @param name 工具名称
     * @param toolType 工具类型
     * @param description 工具描述
     */
    protected Tool(String name, String toolType, String description) {
        this.name = name;
        this.toolType = toolType;
        this.description = description;
    }

    /** @return 工具名称 */
    public String getName() {
        return name;
    }

    /** @return 工具类型 */
    public String getToolType() {
        return toolType;
    }

    /** @return 工具描述 */
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return String.format("%s(name='%s', type='%s')",
            this.getClass().getSimpleName(), name, toolType);
    }
}

