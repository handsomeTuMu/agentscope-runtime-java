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
 * 文件名称: AgentHandler.java
 * 模块: engine-core
 * 包: io.agentscope.runtime.adapters
 *
 * Agent 处理器接口 —— 适配器层的顶层抽象，为不同 Agent 框架（如 AgentScope、
 * Spring AI Alibaba、Langchain4j 等）提供统一的生命周期管理和查询处理契约。
 * Runner 通过此接口与具体的 Agent 框架进行交互，实现框架无关的调度逻辑。
 */
package io.agentscope.runtime.adapters;

import io.agentscope.runtime.engine.schemas.AgentRequest;
import io.agentscope.runtime.sandbox.manager.SandboxService;
import reactor.core.publisher.Flux;

/**
 * Agent 处理器接口，为 AgentApp 的核心能力提供统一抽象层。
 *
 * <p>该接口设计用于适配不同的 Agent 框架类型（如 AgentScope、Spring AI Alibaba、Langchain4j），
 * 提供核心的生命周期管理和查询处理能力。</p>
 *
 * <p>架构位置：该接口是适配器层的顶层接口，每个具体框架的适配器都需要实现此接口。
 * Runner 通过此接口与具体的 Agent 框架进行交互。</p>
 *
 * <p>生命周期流程：</p>
 * <ol>
 *   <li>{@link #start()} - 启动适配器并标记为就绪状态</li>
 *   <li>{@link #streamQuery(AgentRequest, Object)} - 处理查询（可多次调用）</li>
 *   <li>{@link #stop()} - 停止接受新查询</li>
 * </ol>
 *
 */
public interface AgentHandler {
    /** 获取沙箱服务实例，用于隔离执行 Agent 相关的文件系统和进程操作 */
    SandboxService getSandboxService();

    /** 获取 Agent 名称 */
    String getName();

    /** 获取 Agent 描述信息 */
    String getDescription();

    /**
     * 获取该适配器支持的框架类型。
     *
     * <p>支持的框架类型包括："AgentScope"、"Spring Ai Alibaba"、"Langchain4j"</p>
     *
     * @return 框架类型字符串
     */
    String getFrameworkType();

    /**
     * 启动适配器并标记为就绪状态，准备处理查询。
     *
     * <p>该方法完成后，适配器应能通过 {@link #streamQuery(AgentRequest, Object)} 接受查询。</p>
     *
     * <p>该方法通常会调用已注册的 init_handler（初始化处理器）。</p>
     */
    void start();

    /**
     * 处理 Agent 查询并返回框架特定事件的流。
     *
     * <p>处理 Agent 请求并返回框架特定事件的响应式流。</p>
     *
     * <p>实现类应完成以下工作：</p>
     * <ul>
     *   <li>处理框架特定的消息格式转换</li>
     *   <li>调用已注册的查询处理器</li>
     *   <li>返回原始的框架特定事件流（如 AgentScope 的 Flux&lt;io.agentscope.core.agent.Event&gt;）</li>
     * </ul>
     *
     * <p>该方法可被并发多次调用。在调用此方法前，适配器应处于已启动状态
     * （即 {@link #start()} 已执行完成）。</p>
     *
     * <p>messages 参数包含框架特定的已转换消息（如 AgentScope 的 List&lt;Msg&gt;）。
     * 转换后的消息通过 kwargs 传递：kwargs.update({"msgs": message_to_agentscope_msg(request.input)})。
     * 如果 messages 为 null，适配器应直接使用 request.getInput()。</p>
     *
     * <p>注意：返回的流应包含框架特定的 Event 对象。
     * Runner 会使用 StreamAdapter 将此原始框架流转换为运行时 Event 流。</p>
     *
     * @param request 包含输入消息、会话信息等的 Agent 请求
     * @param messages 已转换的框架特定消息（如 AgentScope 的 List&lt;Msg&gt;），为 null 时表示无需转换
     * @return 框架特定 Event 对象的 Flux 流（如 AgentScope 的 Flux&lt;io.agentscope.core.agent.Event&gt;）
     * @throws IllegalStateException 如果适配器未启动
     * @throws RuntimeException 如果查询处理器未设置或遇到错误
     */
    Flux<?> streamQuery(AgentRequest request, Object messages);

    /**
     * 停止适配器，阻止其接受新查询。
     *
     * <p>该方法应优雅地停止处理新查询，同时允许正在进行的查询完成。
     * 该方法完成后，不应再调用 {@link #streamQuery(AgentRequest, Object)}。</p>
     */
    void stop();


    /**
     * 检查适配器是否处于健康/就绪状态。
     *
     * <p>如果适配器已启动且准备好处理查询，则被视为健康状态。</p>
     *
     * @return 如果适配器健康且就绪则返回 true，否则返回 false
     */
    boolean isHealthy();

    /**
     * 获取该框架适配器的 StreamAdapter 实现。
     *
     * <p>该方法返回一个 StreamAdapter 实例，用于将框架特定的流式事件转换为运行时 Message 流。
     * 并非所有适配器都支持流式传输，如果不支持，应返回 null。</p>
     *
     * <p>StreamAdapter 提供：</p>
     * <ul>
     *   <li>通过 {@link StreamAdapter#adaptFrameworkStream(Object)} 进行流转换</li>
     * </ul>
     * <p>两种方法都返回 Flux&lt;Message&gt;，提供响应式、非阻塞的流式支持。</p>
     *
     * @return 该框架的 StreamAdapter 实现，如果不支持流式传输则返回 null
     */
    StreamAdapter getStreamAdapter();

    /**
     * 获取该框架适配器的 MessageAdapter 实现。
     *
     * <p>该方法返回一个 MessageAdapter 实例，用于在框架特定的消息格式
     * 和运行时 Message 对象之间进行转换。</p>
     *
     * <p>MessageAdapter 提供：</p>
     * <ul>
     *   <li>框架到运行时转换：{@link MessageAdapter#frameworkMsgToMessage(Object)}</li>
     *   <li>运行时到框架转换：{@link MessageAdapter#messageToFrameworkMsg(Object)}</li>
     * </ul>
     *
     * @return 该框架的 MessageAdapter 实现
     */
    MessageAdapter getMessageAdapter();
}
