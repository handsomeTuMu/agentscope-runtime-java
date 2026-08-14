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
 * 文件名称: Session.java
 * 模块: engine-core
 * 包: io.agentscope.runtime.engine.schemas
 *
 * 会话模型，表示一个完整的对话会话。
 * 包含会话 ID、用户 ID 和该会话中的所有消息历史。
 * 由 SessionHistoryService 管理其持久化和检索。
 */
package io.agentscope.runtime.engine.schemas;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 会话模型。
 *
 * <p>角色：表示 Agent 与用户之间的一个对话会话，包含会话的唯一标识、
 * 归属用户以及该会话中的完整消息历史列表。</p>
 *
 * <p>设计模式：值对象模式（Value Object）—— 聚合根，聚合了会话中的所有消息。</p>
 */
public class Session {

    /** 会话唯一标识符 */
    @JsonProperty("id")
    private String id;

    /** 会话归属用户 ID */
    @JsonProperty("user_id")
    private String userId;

    /** 会话中的消息历史列表 */
    @JsonProperty("messages")
    private List<Message> messages;
    
    public Session() {}
    
    public Session(String id, String userId) {
        this.id = id;
        this.userId = userId;
        this.messages = new ArrayList<>();
    }
    
    public Session(String id, String userId, List<Message> messages) {
        this.id = id;
        this.userId = userId;
        this.messages = messages;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public List<Message> getMessages() {
        return messages;
    }
    
    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }
}
