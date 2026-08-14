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
 * 文件名称: FunctionCallOutput.java
 * 模块: engine-core
 * 包: io.agentscope.runtime.engine.schemas
 *
 * 函数调用输出模型，表示一次工具/函数调用的执行结果。
 * 通过 call_id 与对应的 {@link FunctionCall} 关联。
 */

package io.agentscope.runtime.engine.schemas;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 函数调用输出模型。
 *
 * <p>角色：表示 Agent 发起的函数调用的执行结果。通过 {@code callId}
 * 与原始的 {@link FunctionCall} 关联，形成调用-响应的配对关系。</p>
 *
 * <p>设计模式：值对象模式（Value Object）—— 命令模式的结果载体。</p>
 */
public class FunctionCallOutput {
    /** 关联的函数调用 ID，与 {@link FunctionCall#getCallId()} 对应 */
    @JsonProperty("call_id")
    private String callId;

    /** 产生此输出的函数/工具名称 */
    @JsonProperty("name")
    private String name;

    /** 函数执行结果输出（通常为 JSON 格式字符串） */
    @JsonProperty("output")
    private String output;
    
    public FunctionCallOutput() {
    }
    
    public FunctionCallOutput(String callId, String name, String output) {
        this.callId = callId;
        this.name = name;
        this.output = output;
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
    
    public String getOutput() {
        return output;
    }
    
    public void setOutput(String output) {
        this.output = output;
    }
}

