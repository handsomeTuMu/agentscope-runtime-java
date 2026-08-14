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
 * 文件名称: DataContent.java
 * 模块: engine-core
 * 包: io.agentscope.runtime.engine.schemas
 *
 * 结构化数据内容类，表示 Agent 消息中的键值对形式的结构化数据。
 * 继承自 {@link Content}，用于传递函数调用参数、工具执行结果等结构化信息。
 */

package io.agentscope.runtime.engine.schemas;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * 结构化数据内容模型。
 *
 * <p>角色：表示 Agent 消息中的结构化数据内容（Map 格式），
 * 用于传递函数调用、工具执行结果等需要结构化表示的信息。</p>
 *
 * <p>设计模式：值对象模式（Value Object）—— 继承 Content 并扩展 Map 数据字段。</p>
 */
public class DataContent extends Content {
    /** 结构化数据内容（键值对形式） */
    @JsonProperty("data")
    private Map<String, Object> data;
    
    public DataContent() {
        this.setType(ContentType.DATA);
    }
    
    public Map<String, Object> getData() {
        return data;
    }
    
    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}

