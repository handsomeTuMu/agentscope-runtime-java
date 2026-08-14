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
 * 文件名称: Role.java
 * 模块: engine-core
 * 包: io.agentscope.runtime.engine.schemas
 *
 * 消息角色常量类，定义 Agent 对话中消息的发送者角色。
 * 这些常量用于 Message 对象的 role 字段。
 */

package io.agentscope.runtime.engine.schemas;

/**
 * 消息角色常量类。
 *
 * <p>角色：集中定义 Agent 对话中所有可能的消息发送者角色常量，
 * 避免���法字符串的散布。这些角色标识消息来源，影响 Agent 的行为。</p>
 *
 * <p>设计模式：常量类（Constants Class）—— 工具类模式，私有构造函数防止实例化。</p>
 */
public class Role {
    /** 助手角色 —— Agent/LLM 生成的回复消息 */
    public static final String ASSISTANT = "assistant";
    /** 用户角色 —— 终端用户发送的输入消息 */
    public static final String USER = "user";
    /** 系统角色 —— 系统指令消息，用于设定 Agent 的行为规范 */
    public static final String SYSTEM = "system";
    /** 工具角色 —— 工具/函数调用的返回结果消息 */
    public static final String TOOL = "tool";
    
    private Role() {
        // Utility class
    }
}

