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
 * 文件名称: Queue.java
 * 模块: engine-core
 * 包: io.agentscope.runtime.common.collections
 *
 * 队列抽象基类，定义了先进先出（FIFO）队列的标准操作接口。
 * 支持入队、出队、查看队首元素等操作，具体实现包括内存版本和 Redis 版本等。
 */

package io.agentscope.runtime.common.collections;

import java.util.Map;

/**
 * 队列抽象基类。
 *
 * <p>角色：定义先进先出（FIFO）队列的标准操作契约，
 * 为 Agent 运行时提供消息队列能力。</p>
 *
 * <p>设计模式：策略模式 —— 不同的后端实现（如 {@link InMemoryQueue}、Redis 等）
 * 提供各自的队列方案，但对外暴露统一的接口。</p>
 */
public abstract class Queue {
    /**
     * Adds an item to the queue.
     * 
     * @param item Item to enqueue (as a Map)
     */
    public abstract void enqueue(Map<String, Object> item);
    
    /**
     * Removes and returns an item from the queue.
     * 
     * @return The dequeued item, or null if queue is empty
     */
    public abstract Map<String, Object> dequeue();
    
    /**
     * Returns the front item without removing it.
     * 
     * @return The front item, or null if queue is empty
     */
    public abstract Map<String, Object> peek();
    
    /**
     * Checks if the queue is empty.
     * 
     * @return true if empty, false otherwise
     */
    public abstract boolean isEmpty();
    
    /**
     * Returns the number of items in the queue.
     * 
     * @return Queue size
     */
    public abstract int size();
}

