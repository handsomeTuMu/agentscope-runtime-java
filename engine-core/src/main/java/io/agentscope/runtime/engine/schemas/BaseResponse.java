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
 * 文件名称: BaseResponse.java
 * 模块: engine-core
 * 包: io.agentscope.runtime.engine.schemas
 *
 * 响应基类，所有 Agent 响应模型的父类。
 * 继承自 {@link Event}，扩展了响应 ID、输出消息列表、Token 用量、时间戳等字段。
 * 响应对象本身也是一种 Event，可以参与流式输出，并经历状态生命周期转换。
 */

package io.agentscope.runtime.engine.schemas;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Agent 响应基类。
 *
 * <p>角色：为所有 Agent 响应模型（如 {@link AgentResponse}）提供公共字段和方法。
 * 包含响应 ID、创建/完成时间戳、输出消息列表和 Token 用量统计。</p>
 *
 * <p>职责：</p>
 * <ul>
 *   <li>管理响应的唯一标识符（自动生成 UUID 前缀）</li>
 *   <li>维护输出消息列表（addNewMessage）</li>
 *   <li>记录响应的创建时间和完成时间</li>
 *   <li>携带 Token 用量统计信息</li>
 *   <li>支持状态生命周期转换（created -> in_progress -> completed/failed）</li>
 * </ul>
 *
 * <p>设计模式：模板方法模式 —— 状态转换方法返回 this，子类可覆盖返回更具体的类型；
 * 流畅接口模式（Fluent Interface）—— 链式调用。</p>
 */
public class BaseResponse extends Event {
    /** 响应唯一标识符 */
    @JsonProperty("id")
    private String id;

    /** 对象类型，固定为 "response" */
    @JsonProperty("object")
    private String object = "response";

    /** 响应创建时间（Unix 时间戳，秒级） */
    @JsonProperty("created_at")
    private Long createdAt;

    /** 响应完成时间（Unix 时间戳，秒级） */
    @JsonProperty("completed_at")
    private Long completedAt;

    /** 输出消息列表，包含 Agent 生成的所有回复消息 */
    @JsonProperty("output")
    private List<Message> output;

    /** Token 使用量统计（如 prompt_tokens、completion_tokens 等） */
    @JsonProperty("usage")
    private Map<String, Object> usage;
    
    public BaseResponse() {
        super();
        this.id = "response_" + UUID.randomUUID().toString();
        this.createdAt = System.currentTimeMillis() / 1000; // Unix timestamp
    }
    
    public BaseResponse(String id) {
        super();
        this.id = id != null ? id : "response_" + UUID.randomUUID().toString();
        this.createdAt = System.currentTimeMillis() / 1000;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getObject() {
        return object;
    }
    
    public void setObject(String object) {
        this.object = object;
    }
    
    public Long getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }
    
    public Long getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(Long completedAt) {
        this.completedAt = completedAt;
    }
    
    public List<Message> getOutput() {
        return output;
    }
    
    public void setOutput(List<Message> output) {
        this.output = output;
    }
    
    public Map<String, Object> getUsage() {
        return usage;
    }
    
    public void setUsage(Map<String, Object> usage) {
        this.usage = usage;
    }
    
    /**
     * Add a new message to the output list.
     */
    public void addNewMessage(Message message) {
        if (this.output == null) {
            this.output = new java.util.ArrayList<>();
        }
        this.output.add(message);
    }
    
    /**
     * Set status to created and return self.
     */
    public BaseResponse created() {
        super.created();
        return this;
    }
    
    /**
     * Set status to in_progress and return self.
     */
    public BaseResponse inProgress() {
        super.inProgress();
        return this;
    }
    
    /**
     * Set status to completed and return self.
     */
    public BaseResponse completed() {
        super.completed();
        this.completedAt = System.currentTimeMillis() / 1000;
        return this;
    }
    
    /**
     * Set status to failed with error and return self.
     */
    public BaseResponse failed(Error error) {
        super.failed(error);
        return this;
    }
}

