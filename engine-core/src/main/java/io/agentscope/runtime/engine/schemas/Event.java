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
 * 文件名称: Event.java
 * 模块: engine-core
 * 包: io.agentscope.runtime.engine.schemas
 *
 * 事件基类，是 Agent 运行时中所有事件类型的根类。
 * Message 和 Content 都继承自此类，使得它们可以作为统一的 Event 流式输出。
 * 包含序列号、对象类型、状态和错误信息等公共字段。
 */

package io.agentscope.runtime.engine.schemas;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 事件基类 —— Agent 运行时中所有事件类型的根基类。
 *
 * <p>角色：为 Message、Content 等具体事件类型提供公共字段和状态管理方法。
 * 通过继承体系，使得 Runner 可以统一地以 Flux&lt;Event&gt; 形式输出不同类型的事件。</p>
 *
 * <p>设计模式：模板方法模式 —— 状态转换方法（created、inProgress、completed 等）
 * 返回 this，支持链式调用，子类可以覆盖以返回更具体的类型。</p>
 */
public class Event {
    /** 事件序列号，用于排序和去重 */
    @JsonProperty("sequence_number")
    private Integer sequenceNumber;

    /** 对象类型（如 "response"、"message"、"content" 等） */
    @JsonProperty("object")
    private String object;

    /** 事件运行状态（参见 {@link RunStatus}） */
    @JsonProperty("status")
    protected String status;

    /** 错误信息（仅失败状态下使用） */
    @JsonProperty("error")
    private Error error;
    
    /** @return 事件序列号 */
    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

    /** @param sequenceNumber 事件序列号 */
    public void setSequenceNumber(Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    /** @return 对象类型 */
    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    /** @return 事件运行状态 */
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /** @return 错误信息 */
    public Error getError() {
        return error;
    }

    public void setError(Error error) {
        this.error = error;
    }

    /** 将事件状态设置为 "created"（已创建）。 @return 当前事件对象 */
    public Event created() {
        this.status = RunStatus.CREATED;
        return this;
    }

    /** 将事件状态设置为 "in_progress"（处理中）。 @return 当前事件对象 */
    public Event inProgress() {
        this.status = RunStatus.IN_PROGRESS;
        return this;
    }

    /** 将事件状态设置为 "completed"（已完成）。 @return 当前事件对象 */
    public Event completed() {
        this.status = RunStatus.COMPLETED;
        return this;
    }

    /** 将事件状态设置为 "failed"（失败），并附带错误信息。 @return 当前事件对象 */
    public Event failed(Error error) {
        this.status = RunStatus.FAILED;
        this.error = error;
        return this;
    }

    /** 将事件状态设置为 "rejected"（已拒绝）。 @return 当前事件对象 */
    public Event rejected() {
        this.status = RunStatus.REJECTED;
        return this;
    }

    /** 将事件状态设置为 "canceled"（已取消）。 @return 当前事件对象 */
    public Event canceled() {
        this.status = RunStatus.CANCELED;
        return this;
    }
}

