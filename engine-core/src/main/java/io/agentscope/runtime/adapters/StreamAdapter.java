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
 * 文件名称: StreamAdapter.java
 * 模块: engine-core
 * 包: io.agentscope.runtime.adapters
 *
 * 流式适配器接口，将不同 Agent 框架的流式事件格式适配为运行时统一的 Event 流。
 * 是连接具体 Agent 框架流式输出与运行时核心引擎的桥梁，
 * 使 Runner 能够以统一方式处理来自不同框架的流式响应。
 */
package io.agentscope.runtime.adapters;

import io.agentscope.runtime.engine.schemas.Event;
import reactor.core.publisher.Flux;

/**
 * 流式适配器接口，提供统一抽象层，用于在框架特定的流式事件格式和运行时 Event 流之间进行转换。
 *
 * <p>该接口设计用于将不同 Agent 框架的流式事件类型（如 AgentScope Event、AutoGen 流式事件等）
 * 适配为运行时 Event 流。返回的流可以同时包含 Message 和 Content 对象，
 * 因为两者都继承自 Event。</p>
 *
 * <p>架构位置：该接口位于适配器层（adapters），是连接具体 Agent 框架与运行时核心引擎的桥梁。</p>
 *
 * <p>实现类应处理以下职责：</p>
 * <ul>
 *   <li>将框架特定的事件流转换为运行时 Event 流</li>
 *   <li>使用 Flux 处理响应式流式模型</li>
 *   <li>在转换后的事件中保留流式状态（进行中、已完成）</li>
 *   <li>根据增量更新的需要，同时产出 Message 和 Content 对象</li>
 * </ul>
 */
public interface StreamAdapter {

    /**
     * 将框架特定的事件流适配为运行时 Event 流。
     *
     * <p>该方法将框架特定的事件流转换为 Reactor Flux 形式的运行时 Event 对象
     * （可以是 Message 或 Content），提供响应式、非阻塞的流式支持。</p>
     *
     * @param sourceStream 框架特定的事件流（类型取决于具体框架）
     * @return 运行时 Event 对象的 Flux 流（Message 或 Content）
     */
    Flux<Event> adaptFrameworkStream(Object sourceStream);

}

