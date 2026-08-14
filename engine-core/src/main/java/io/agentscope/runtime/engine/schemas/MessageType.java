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
 * 文件名称: MessageType.java
 * 模块: engine-core
 * 包: io.agentscope.runtime.engine.schemas
 *
 * 消息类型常量类，定义 Agent 运行时中所有可能的消息类型。
 * 这些类型用于 Message 对象的 type 字段，标识消息的语义类别。
 */

package io.agentscope.runtime.engine.schemas;

import java.util.Arrays;
import java.util.List;

/**
 * 消息类型常量类。
 *
 * <p>角色：集中定义 Agent 运行时中所有可能的消息类型常量。
 * 消息类型决定了消息的语义和处理方式，如普通对话消息、函数调用、
 * 工具执行结果、MCP 相关消息等。</p>
 *
 * <p>设计模式：常量类（Constants Class）—— 工具类模式，私有构造函数防止实例化。</p>
 */
public class MessageType {
    /** 普通对话消息 */
    public static final String MESSAGE = "message";
    /** 函数调用消息 —— Agent 请求调用某个函数/工具 */
    public static final String FUNCTION_CALL = "function_call";
    /** 函数调用输出消息 —— 函数/工具的执行结果 */
    public static final String FUNCTION_CALL_OUTPUT = "function_call_output";
    /** 插件调用消息 */
    public static final String PLUGIN_CALL = "plugin_call";
    /** 插件调用输出消息 */
    public static final String PLUGIN_CALL_OUTPUT = "plugin_call_output";
    /** 组件调用消息 */
    public static final String COMPONENT_CALL = "component_call";
    /** 组件调用输出消息 */
    public static final String COMPONENT_CALL_OUTPUT = "component_call_output";
    /** MCP 列出工具消息 */
    public static final String MCP_LIST_TOOLS = "mcp_list_tools";
    /** MCP 审批请求消息 */
    public static final String MCP_APPROVAL_REQUEST = "mcp_approval_request";
    /** MCP 工具调用消息 */
    public static final String MCP_TOOL_CALL = "mcp_call";
    /** MCP 审批响应消息 */
    public static final String MCP_APPROVAL_RESPONSE = "mcp_approval_response";
    /** 推理过程消息 —— 展示 Agent 的推理/思考过程 */
    public static final String REASONING = "reasoning";
    /** 心跳消息 —— 用于保活和状态同步 */
    public static final String HEARTBEAT = "heartbeat";
    /** 错误消息 */
    public static final String ERROR = "error";
    
    /**
     * Returns all message type values.
     */
    public static List<String> allValues() {
        return Arrays.asList(
            MESSAGE,
            FUNCTION_CALL,
            FUNCTION_CALL_OUTPUT,
            PLUGIN_CALL,
            PLUGIN_CALL_OUTPUT,
            COMPONENT_CALL,
            COMPONENT_CALL_OUTPUT,
            MCP_LIST_TOOLS,
            MCP_APPROVAL_REQUEST,
            MCP_TOOL_CALL,
            MCP_APPROVAL_RESPONSE,
            REASONING,
            HEARTBEAT,
            ERROR
        );
    }
    
    private MessageType() {
        // Utility class
    }
}

