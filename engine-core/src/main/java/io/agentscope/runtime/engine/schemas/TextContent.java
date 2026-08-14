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
 * 文件名称: TextContent.java
 * 模块: engine-core
 * 包: io.agentscope.runtime.engine.schemas
 *
 * 文本内容类，表示 Agent 消息中的纯文本内容片段。
 * 继承自 {@link Content}，是 Agent 回复中最常见的内容类型。
 * 支持增量文本拼接，用于流式输出场景。
 */

package io.agentscope.runtime.engine.schemas;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 文本内容模型。
 *
 * <p>角色：表示 Agent 消息中的一段文本内容，如 Agent 的回复文本、
 * 工具调用的描述信息等。是 {@link Content} 的最常用子类。</p>
 *
 * <p>设计模式：值对象模式（Value Object）—— 继承 Content 并扩展文本字段；
 * 增量模式 —— 支持通过 delta 标志实现流式文本拼接。</p>
 */
public class TextContent extends Content {
    /** 文本内容 */
    @JsonProperty("text")
    private String text;
    
    public TextContent() {
        this.setType(ContentType.TEXT);
    }
    
    public TextContent(String text) {
        this();
        this.text = text;
    }
    
    public TextContent(Boolean delta, Integer index, String text) {
        this(text);
        this.setDelta(delta);
        this.setIndex(index);
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
}

