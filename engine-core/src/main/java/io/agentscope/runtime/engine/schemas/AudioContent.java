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
 * 文件名称: AudioContent.java
 * 模块: engine-core
 * 包: io.agentscope.runtime.engine.schemas
 *
 * 音频内容类，表示 Agent 消息中的音频内容片段。
 * 继承自 {@link Content}，包含音频数据（Base64 编码）和格式信息。
 */

package io.agentscope.runtime.engine.schemas;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 音频内容模型。
 *
 * <p>角色：表示 Agent 消息中的音频内容，携带 Base64 编码的音频数据和格式标识。
 * 用于在对话中传递语音输入、音频回复等多媒体信息。</p>
 *
 * <p>设计模式：值对象模式（Value Object）—— 继承 Content 并扩展音频数据和格式字段。</p>
 */
public class AudioContent extends Content {
    /** 音频数据（通常为 Base64 编码的字符串） */
    @JsonProperty("data")
    private String data;

    /** 音频格式（如 "wav"、"mp3"、"pcm" 等） */
    @JsonProperty("format")
    private String format;
    
    public AudioContent() {
        this.setType(ContentType.AUDIO);
    }
    
    public String getData() {
        return data;
    }
    
    public void setData(String data) {
        this.data = data;
    }
    
    public String getFormat() {
        return format;
    }
    
    public void setFormat(String format) {
        this.format = format;
    }
}

