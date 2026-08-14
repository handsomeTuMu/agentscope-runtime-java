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
 * 文件名称: FunctionCall.java
 * 模块: engine-core
 * 包: io.agentscope.runtime.engine.schemas
 *
 * 函数调用模型，表示 Agent 发起的一次工具/函数调用请求。
 * 包含调用 ID、函数名和参数信息，用于 Agent 与外部工具的交互。
 */

package io.agentscope.runtime.engine.schemas;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 函数调用模型。
 *
 * <p>角色：表示 Agent 在对话过程中发起的一次函数/工具调用请求。
 * Agent 通过此对象指定要调用的函数名称和参数，运行时根据这些信息
 * 路由到具体的工具实现并执行。</p>
 *
 * <p>设计模式：值对象模式（Value Object）—— 命令模式的数据载体，
 * 封装了函数调用的全部信息。</p>
 */
public class FunctionCall {
    /** 调用唯一标识符，用于关联后续的 {@link FunctionCallOutput} */
    @JsonProperty("call_id")
    private String callId;

    /** 要调用的函数/工具名称 */
    @JsonProperty("name")
    private String name;

    /** 函数参数（JSON 格式字符串） */
    @JsonProperty("arguments")
    private String arguments;
    
    public FunctionCall() {
    }
    
    public FunctionCall(String callId, String name, String arguments) {
        this.callId = callId;
        this.name = name;
        this.arguments = arguments;
    }
    
    public String getCallId() {
        return callId;
    }
    
    public void setCallId(String callId) {
        this.callId = callId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getArguments() {
        return arguments;
    }
    
    public void setArguments(String arguments) {
        this.arguments = arguments;
    }
}

