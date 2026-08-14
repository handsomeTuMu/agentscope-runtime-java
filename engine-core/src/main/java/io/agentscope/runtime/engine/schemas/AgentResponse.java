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
 * 文件名称: AgentResponse.java
 * 模块: engine-core
 * 包: io.agentscope.runtime.engine.schemas
 *
 * Agent 响应模型，表示 Agent 对查询请求的完整响应。
 * 继承自 {@link BaseResponse}，扩展了会话 ID 字段用于对话追踪。
 * 响应对象本身也是一种 Event，可以参与流式输出。
 */

package io.agentscope.runtime.engine.schemas;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Agent 响应模型。
 *
 * <p>角色：表示 Agent 对一次查询请求的完整响应，包含输出消息列表、
 * 会话 ID、状态、Token 用量等信息。在流式输出中，Response 对象会
 * 经历 created -> in_progress -> completed/failed 的状态转换。</p>
 *
 * <p>设计模式：值对象模式（Value Object）—— DTO 模式的响应载体；
 * 状态转换方法返回 this，支持链式调用（流畅接口模式）。</p>
 */
public class AgentResponse extends BaseResponse {
    /** 会话 ID，用于关联同一对话会话中的多个请求和响应 */
    @JsonProperty("session_id")
    private String sessionId;

    /** 默认构造函数 */
    public AgentResponse() {
        super();
    }

    /**
     * 带响应 ID 的构造函数。
     *
     * @param id 响应唯一标识符
     */
    public AgentResponse(String id) {
        super(id);
    }

    /**
     * 带响应 ID 和会话 ID 的构造函数。
     *
     * @param id 响应唯一标识符
     * @param sessionId 会话 ID
     */
    public AgentResponse(String id, String sessionId) {
        super(id);
        this.sessionId = sessionId;
    }

    /**
     * 带响应 ID、会话 ID 和创建时间的完整构造函数。
     *
     * @param id 响应唯一标识符
     * @param sessionId 会话 ID
     * @param createdAt 创建时间戳（Unix 秒）
     */
    public AgentResponse(String id, String sessionId, Long createdAt) {
        super(id);
        this.sessionId = sessionId;
        this.setCreatedAt(createdAt);
    }

    /** @return 会话 ID */
    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * 将响应状态设置为 "created"（已创建）。
     *
     * @return 当前响应对象（支持链式调用）
     */
    @Override
    public AgentResponse created() {
        super.created();
        return this;
    }

    /**
     * 将响应状态设置为 "in_progress"（处理中）。
     *
     * @return 当前响应对象（支持链式调用）
     */
    @Override
    public AgentResponse inProgress() {
        super.inProgress();
        return this;
    }

    /**
     * 将响应状态设置为 "completed"（已完成）。
     *
     * @return 当前响应对象（支持链式调用）
     */
    @Override
    public AgentResponse completed() {
        super.completed();
        return this;
    }

    /**
     * 将响应状态设置为 "failed"（失败），并附带错误信息。
     *
     * @param error 错误信息对象
     * @return 当前响应对象（支持链式调用）
     */
    @Override
    public AgentResponse failed(Error error) {
        super.failed(error);
        return this;
    }
}

