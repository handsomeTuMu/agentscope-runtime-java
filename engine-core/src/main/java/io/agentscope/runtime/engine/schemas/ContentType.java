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
 * 文件名称: ContentType.java
 * 模块: engine-core
 * 包: io.agentscope.runtime.engine.schemas
 *
 * 内容类型常量类，定义消息内容支持的所有类型。
 */

package io.agentscope.runtime.engine.schemas;

/**
 * 内容类型常量类。
 *
 * <p>角色：集中定义消息内容（Content）支持的所有类型常量，
 * 用于在 ContentBuilder 中指定要构建的内容类型。</p>
 *
 * <p>设计模式：常量类（Constants Class）—— 工具类模式，私有构造函数防止实例化。</p>
 */
public class ContentType {
    /** 文本类型 —— 纯文本内容，支持增量分片 */
    public static final String TEXT = "text";
    /** 数据类型 —— 结构化数据内容（Map 格式），支持增量合并 */
    public static final String DATA = "data";
    /** 图片类型 —— 图片内容（通过 URL 引用） */
    public static final String IMAGE = "image";
    /** 音频类型 —— 音频内容 */
    public static final String AUDIO = "audio";
    /** 文件类型 —— 文件内容 */
    public static final String FILE = "file";
    /** 拒绝类型 —— Agent 拒绝响应的内容 */
    public static final String REFUSAL = "refusal";

    /** 私有构造函数，防止实例化 */
    private ContentType() {
        // 工具类，不允许实例化
    }
}

