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
 * 文件名称: AgentRequest.java
 * 模块: engine-core
 * 包: io.agentscope.runtime.engine.schemas
 *
 * Agent 请求模型，表示客户端发给 Agent 的查询请求。
 * 继承自 {@link BaseRequest}，扩展了模型参数、输入消息列表、会话 ID 等字段。
 */

package io.agentscope.runtime.engine.schemas;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Agent 请求模型。
 *
 * <p>角色：表示客户端向 Agent 发送的查询请求，包含输入消息、会话标识、
 * 模型参数等信息。该对象由 Web 层构建，传递给 Runner 进行处理。</p>
 *
 * <p>设计模式：值对象模式（Value Object）—— DTO 模式的请求载体。</p>
 */
public class AgentRequest extends BaseRequest {
    /** 模型名称（如 "gpt-4"、"deepseek-chat" 等） */
    @JsonProperty("model")
    private String model;

    /** Top-P 采样参数，控制生成多样性 */
    @JsonProperty("top_p")
    private Double topP;

    /** 温度参数，控制生成随机性 */
    @JsonProperty("temperature")
    private Double temperature;

    /** 输入消息列表，即用户发送给 Agent 的对话消息 */
    @JsonProperty("input")
    private List<Message> input;

    /** 会话 ID，用于标识一个对话会话 */
    @JsonProperty("session_id")
    private String sessionId;

    /** 用户 ID，标识发起请求的用户 */
    @JsonProperty("user_id")
    private String userId;

    // ===== Getter 和 Setter 方法 =====

    /** @return 模型名称 */
    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    /** @return Top-P 采样参数 */
    public Double getTopP() {
        return topP;
    }

    public void setTopP(Double topP) {
        this.topP = topP;
    }

    /** @return 温度参数 */
    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    /** @return 输入消息列表 */
    public List<Message> getInput() {
        return input;
    }

    public void setInput(List<Message> input) {
        this.input = input;
    }

    /** @return 会话 ID */
    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    /** @return 用户 ID */
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}

