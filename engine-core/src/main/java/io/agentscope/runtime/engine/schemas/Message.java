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
 * 文件名称: Message.java
 * 模块: engine-core
 * 包: io.agentscope.runtime.engine.schemas
 *
 * 消息对象模型，表示 Agent 运行时中的一条消息。
 * 继承自 {@link Event}，是 Agent 对话中消息传递的标准载体。
 * 包含角色（role）、内容列表（content）、Token 用量（usage）等核心字段。
 */

package io.agentscope.runtime.engine.schemas;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Agent 运行时消息模型。
 *
 * <p>角色：表示 Agent 对话中的一条消息，可以是用户输入、Agent 回复、
 * 工具调用结果等不同角色的消息。</p>
 *
 * <p>职责：</p>
 * <ul>
 *   <li>携带消息 ID、角色、类型、内容列表等元数据</li>
 *   <li>管理消息的生命周期状态（created、in_progress、completed）</li>
 *   <li>支持增量内容的合并（addDeltaContent）</li>
 *   <li>携带 Token 用量统计和自定义元数据</li>
 * </ul>
 *
 * <p>设计模式：值对象模式（Value Object）—— 表示领域中的一个数据载体。</p>
 */
public class Message extends Event {
    /** 消息唯一标识符 */
    @JsonProperty("id")
    private String id;

    /** 对象类型，固定为 "message" */
    @JsonProperty("object")
    private String object = "message";

    /** 消息类型（如 "message"、"function_call" 等） */
    @JsonProperty("type")
    private String type = MessageType.MESSAGE;

    /** 消息角色（如 "user"、"assistant"、"system"） */
    @JsonProperty("role")
    private String role;

    /** 消息内容列表，支持文本、图片、数据等多种内容类型 */
    @JsonProperty("content")
    private List<Content> content;

    /** 错误码（错误消息时使用） */
    @JsonProperty("code")
    private String code;

    /** 错误消息文本（错误消息时使用） */
    @JsonProperty("message")
    private String message;

    /** Token 使用量统计（如 prompt_tokens、completion_tokens 等） */
    @JsonProperty("usage")
    private Map<String, Object> usage;

    /** 自定义元数据 */
    @JsonProperty("metadata")
    private Map<String, Object> metadata;
    
    /**
     * 默认构造函数，自动生成消息 ID。
     */
    public Message() {
        this.id = "msg_" + UUID.randomUUID().toString();
        this.content = new ArrayList<>();
        this.status = RunStatus.CREATED;
    }

    /**
     * 带类型和角色的构造函数。
     *
     * @param type 消息类型
     * @param role 消息角色
     */
    public Message(String type, String role) {
        this();
        this.type = type;
        this.role = role;
    }

    // ===== Getter 和 Setter 方法 =====

    /** @return 消息唯一标识符 */
    public String getId() {
        return id;
    }

    /** @param id 消息唯一标识符 */
    public void setId(String id) {
        this.id = id;
    }

    /** @return 对象类型 */
    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    /** @return 消息类型 */
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /** @return 消息角色 */
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    /** @return 内容列表 */
    public List<Content> getContent() {
        return content;
    }

    public void setContent(List<Content> content) {
        this.content = content;
    }

    /** @return 错误码 */
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    /** @return 错误消息文本 */
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /** @return Token 使用量统计 */
    public Map<String, Object> getUsage() {
        return usage;
    }

    public void setUsage(Map<String, Object> usage) {
        this.usage = usage;
    }

    /** @return 自定义元数据 */
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    /**
     * 添加增量内容到消息中。
     * 如果存在相同索引的同类型内容，则进行合并（如文本拼接）；
     * 否则将新内容添加到内容列表末尾。
     *
     * @param newContent 要添加的增量内容
     * @return 合并后的内容对象
     */
    public Content addDeltaContent(Content newContent) {
        if (content == null) {
            content = new ArrayList<>();
        }

        // 查找相同索引的内容或添加新内容
        int index = newContent.getIndex() != null ? newContent.getIndex() : content.size();
        newContent.setIndex(index);

        if (index < content.size()) {
            // 更新已有内容
            Content existing = content.get(index);
            if (existing.getType().equals(newContent.getType())) {
                // 合并同类型内容
                if (newContent instanceof TextContent && existing instanceof TextContent) {
                    TextContent textContent = (TextContent) existing;
                    TextContent newTextContent = (TextContent) newContent;
                    textContent.setText(textContent.getText() + newTextContent.getText());
                }
                return existing;
            }
        }

        // 添加新内容
        content.add(newContent);
        return newContent;
    }

    /**
     * 将消息状态标记为 "in_progress"（处理中）。
     *
     * @return 当前消息对象（支持链式调用）
     */
    public Message inProgress() {
        this.status = RunStatus.IN_PROGRESS;
        return this;
    }

    /**
     * 将消息状态标记为 "completed"（已完成）。
     *
     * @return 当前消息对象（支持链式调用）
     */
    public Message completed() {
        this.status = RunStatus.COMPLETED;
        return this;
    }
}

