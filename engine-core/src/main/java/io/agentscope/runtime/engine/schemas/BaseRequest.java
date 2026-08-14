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
 * 文件名称: BaseRequest.java
 * 模块: engine-core
 * 包: io.agentscope.runtime.engine.schemas
 *
 * 请求基类，所有 Agent 请求模型的父类。
 * 仅包含请求 ID 字段，具体的请求类型（如 AgentRequest）通过继承扩展更多字段。
 */

package io.agentscope.runtime.engine.schemas;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 请求基类。
 *
 * <p>角色：为所有 Agent 请求模型提供公共的请求 ID 字段。
 * {@link AgentRequest} 继承此类并扩展了输入消息、会话 ID、模型参数等字段。</p>
 *
 * <p>设计模式：值对象模式（Value Object）—— DTO 模式的请求载体基类。</p>
 */
public class BaseRequest {
    /** 请求唯一标识符 */
    @JsonProperty("id")
    private String id;
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
}

