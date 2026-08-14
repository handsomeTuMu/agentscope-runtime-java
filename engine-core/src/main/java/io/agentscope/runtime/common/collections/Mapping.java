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
 * 文件名称: Mapping.java
 * 模块: engine-core
 * 包: io.agentscope.runtime.common.collections
 *
 * 键值对映射抽象基类，定义了通用的 key-value 存储操作接口。
 * 支持设置、获取、删除和前缀扫描操作，具体实现包括内存版本和 Redis 版本等。
 */

package io.agentscope.runtime.common.collections;

import java.util.List;

/**
 * 键值对映射抽象基类。
 *
 * <p>角色：定义通用的 key-value 存储操作契约，为 Agent 运行时提供
 * 状态数据的持久化能力。支持设置、获取、删除和按前缀扫描键值对。</p>
 *
 * <p>设计模式：策略模式 —— 不同的后端实现（如 {@link InMemoryMapping}、Redis 等）
 * 提供各自的存储方案，但对外暴露统一的接口。</p>
 */
public abstract class Mapping {
    /**
     * Sets a key-value pair.
     * 
     * @param key The key
     * @param value The value (as a Map)
     */
    public abstract void set(String key, Object value);
    
    /**
     * Gets the value for a key.
     * 
     * @param key The key
     * @return The value, or null if not found
     */
    public abstract Object get(String key);
    
    /**
     * Deletes a key-value pair.
     * 
     * @param key The key to delete
     */
    public abstract void delete(String key);
    
    /**
     * Scans for keys with the given prefix.
     * 
     * @param prefix The prefix to match
     * @return List of matching keys
     */
    public abstract List<String> scan(String prefix);
}

