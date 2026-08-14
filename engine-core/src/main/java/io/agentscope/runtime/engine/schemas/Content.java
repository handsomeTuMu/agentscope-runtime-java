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
 * 文件名称: Content.java
 * 模块: engine-core
 * 包: io.agentscope.runtime.engine.schemas
 *
 * 消息内容基类，表示 Agent 消息中的一种内容片段（文本、图片、音频等）。
 * 继承自 {@link Event}，使得内容片段也可以作为流式事件输出。
 * 每条 Message 可以包含多个 Content 对象，支持增量合并（delta 机制）。
 */

package io.agentscope.runtime.engine.schemas;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 消息内容基类。
 *
 * <p>角色：表示 Agent 消息中的一种内容片段。具体的内容由子类实现，
 * 如 {@link TextContent}（文本内容）、{@link ImageContent}（图片内容）、
 * {@link AudioContent}（音频内容）、{@link DataContent}（结构化数据内容）等。</p>
 *
 * <p>职责：</p>
 * <ul>
 *   <li>管理内容类型（type）、索引（index）、增量标志（delta）等元数据</li>
 *   <li>关联所属消息 ID（msg_id），用于流式输出时的消息归属判断</li>
 *   <li>支持内容生命周期状态转换（created -> in_progress -> completed）</li>
 * </ul>
 *
 * <p>设计模式：模板方法模式 —— 状态转换方法返回 this，子类可覆盖返回更具体的类型。</p>
 */
public class Content extends Event {
    /** 内容类型（如 "text"、"image"、"audio"、"data" 等），参见 {@link ContentType} */
    @JsonProperty("type")
    private String type;

    /** 对象类型，固定为 "content" */
    @JsonProperty("object")
    private String object = "content";

    /** 内容索引，用于在 Message 的内容列表中定位此内容片段 */
    @JsonProperty("index")
    private Integer index;

    /** 增量标志，true 表示这是一个增量内容片段（流式输出），false 表示内容已完成 */
    @JsonProperty("delta")
    private Boolean delta;

    /** 所属消息 ID，用于关联此内容属于哪条消息 */
    @JsonProperty("msg_id")
    private String msgId;
    
    public Content() {
        super();
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getObject() {
        return object;
    }
    
    public void setObject(String object) {
        this.object = object;
    }
    
    public Integer getIndex() {
        return index;
    }
    
    public void setIndex(Integer index) {
        this.index = index;
    }
    
    public Boolean getDelta() {
        return delta;
    }
    
    public void setDelta(Boolean delta) {
        this.delta = delta;
    }
    
    public String getMsgId() {
        return msgId;
    }
    
    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }
    
    public Content completed() {
        this.delta = false;
        super.completed();
        return this;
    }
    
    public Content inProgress() {
        super.inProgress();
        return this;
    }
}

