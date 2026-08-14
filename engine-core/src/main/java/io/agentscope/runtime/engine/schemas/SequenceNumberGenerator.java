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
 * 文件名称: SequenceNumberGenerator.java
 * 模块: engine-core
 * 包: io.agentscope.runtime.engine.schemas
 *
 * 序列号生成器，为流式事件生成递增的序列号。
 * 用于保证客户端收到的事件顺序有序，支持事件的排序和去重。
 */

package io.agentscope.runtime.engine.schemas;

/**
 * 流式事件序列号生成器。
 *
 * <p>角色：在流式输出场景中，为每个事件分配递增的序列号，
 * 确保客户端能够按正确顺序处理事件，并支持事件去重。</p>
 *
 * <p>设计模式：生成器模式 —— 封装序列号生成的逻辑，
 * 使代码更可维护且不易出错。</p>
 *
 * <p><b>线程安全说明：</b>此类非线程安全。在多线程环境中使用时，
 * 需要外部同步或改用 AtomicInteger。</p>
 */
public class SequenceNumberGenerator {
    /** 当前序列号值 */
    private int current;
    
    /**
     * Initialize the generator with a starting number.
     */
    public SequenceNumberGenerator() {
        this(0);
    }
    
    /**
     * Initialize the generator with a starting number.
     * 
     * @param start The starting sequence number
     */
    public SequenceNumberGenerator(int start) {
        this.current = start;
    }
    
    /**
     * Get the next sequence number and increment the counter.
     * 
     * @return The current sequence number before incrementing
     */
    public int next() {
        int result = current;
        current++;
        return result;
    }
    
    /**
     * Set the sequence number on an event and increment the counter.
     * 
     * @param event The event to set the sequence number on
     * @return The same event with sequence number set
     */
    public Event yieldWithSequence(Event event) {
        event.setSequenceNumber(this.next());
        return event;
    }
}

