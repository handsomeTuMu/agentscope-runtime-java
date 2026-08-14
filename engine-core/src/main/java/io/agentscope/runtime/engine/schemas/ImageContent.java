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
 * 文件名称: ImageContent.java
 * 模块: engine-core
 * 包: io.agentscope.runtime.engine.schemas
 *
 * 图片内容类，表示 Agent 消息中的图片内容片段。
 * 继承自 {@link Content}，通过 URL 引用图片资源。
 */

package io.agentscope.runtime.engine.schemas;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 图片内容模型。
 *
 * <p>角色：表示 Agent 消息中的图片内容，通过图片 URL 引用外部图片资源。
 * 用于在对话中传递截图、图表等视觉信息。</p>
 *
 * <p>设计模式：值对象模式（Value Object）—— 继承 Content 并扩展图片 URL 字段。</p>
 */
public class ImageContent extends Content {
    /** 图片 URL 地址 */
    @JsonProperty("image_url")
    private String imageUrl;
    
    public ImageContent() {
        this.setType(ContentType.IMAGE);
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}

