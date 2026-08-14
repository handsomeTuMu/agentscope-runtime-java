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
 * 文件名称: MessageAdapter.java
 * 模块: engine-core
 * 包: io.agentscope.runtime.adapters
 *
 * 消息适配器接口，在框架特定的消息格式（如 AgentScope Msg、AutoGen Message）
 * 与运行时标准化 Message 对象之间提供双向转换能力。
 * 是实现框架无关性的关键组件之一。
 */
package io.agentscope.runtime.adapters;

import io.agentscope.runtime.engine.schemas.Message;

import java.util.List;

/**
 * 消息适配器接口，提供统一抽象层，用于在框架特定的消息格式和运行时 Message 对象之间进行转换。
 *
 * <p>该接口设计用于将不同 Agent 框架的消息类型（如 AgentScope Msg、AutoGen Message 等）
 * 与运行时标准化 Message 格式之间进行双向转换。</p>
 *
 * <p>架构位置：该接口位于适配器层（adapters），是消息格式标准化转换的核心抽象。</p>
 *
 * <p>实现类应处理以下职责：</p>
 * <ul>
 *   <li>将框架特定消息转换为运行时 Message 对象</li>
 *   <li>将运行时 Message 对象转换回框架特定格式</li>
 *   <li>同时支持单条消息和消息列表的处理</li>
 * </ul>
 */
public interface MessageAdapter {

    /**
     * 将框架特定的消息（单条或列表）转换为一个或多个运行时 Message 对象。
     *
     * <p>该方法接受单条框架消息或消息列表，返回运行时 Message 对象列表。
     * 转换过程应保留原始消息中所有相关的元数据和内容。</p>
     *
     * @param frameworkMsg 框架特定的消息对象或消息对象列表
     * @return 运行时 Message 对象列表
     * @throws IllegalArgumentException 如果输入类型不符合预期
     */
    List<Message> frameworkMsgToMessage(Object frameworkMsg);

    /**
     * 将运行时 Message（单条或列表）转换为框架特定的消息格式。
     *
     * <p>该方法接受单条运行时 Message 或 Message 列表，返回对应的框架特定消息。
     * 返回类型为 Object，以允许实现类根据输入和框架需求返回单条消息或消息列表。</p>
     *
     * @param messages 运行时 Message（单条 Message 或 List&lt;Message&gt;）
     * @return 框架特定的消息对象（单条消息或消息列表）
     * @throws IllegalArgumentException 如果输入类型不符合预期
     */
    Object messageToFrameworkMsg(Object messages);
}

