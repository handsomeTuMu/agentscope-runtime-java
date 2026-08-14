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
 * 文件名称: Error.java
 * 模块: engine-core
 * 包: io.agentscope.runtime.engine.schemas
 *
 * 错误信息模型，表示 Agent 运行时中发生的错误。
 * 包含错误码和错误消息两个字段，用于在 Event、Message 和 Response 中携带错误信息。
 */

package io.agentscope.runtime.engine.schemas;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 错误信息模型。
 *
 * <p>角色：在 Agent 运行时的各种事件（Event、Message、Response）中，
 * 当处理失败时携带标准化的错误信息，包含错误码和可读的错误描述。</p>
 *
 * <p>设计模式：值对象模式（Value Object）—— 简单的数据载体。</p>
 */
public class Error {
    /** 错误码（如 "400"、"500"、"internal_error" 等） */
    @JsonProperty("code")
    private String code;

    /** 错误消息文本，人类可读的错误描述 */
    @JsonProperty("message")
    private String message;
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}

