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
 * 文件名称: Runner.java
 * 模块: engine-core
 * 包: io.agentscope.runtime.engine
 *
 * Agent 运行器核心类。Runner 是 Agent 引擎的核心调度入口，负责将外部请求
 * 代理到底层的 AgentHandler（适配器），并管理 Agent 的完整生命周期与流式查询处理。
 * 该类是连接 Web 层（REST API）与底层 Agent 框架的桥梁。
 */

package io.agentscope.runtime.engine;

import io.agentscope.runtime.adapters.AgentHandler;
import io.agentscope.runtime.adapters.MessageAdapter;
import io.agentscope.runtime.adapters.StreamAdapter;
import io.agentscope.runtime.engine.schemas.AgentRequest;
import io.agentscope.runtime.engine.schemas.AgentResponse;
import io.agentscope.runtime.engine.schemas.Error;
import io.agentscope.runtime.engine.schemas.Event;
import io.agentscope.runtime.engine.schemas.Message;
import io.agentscope.runtime.engine.schemas.RunStatus;
import io.agentscope.runtime.sandbox.manager.SandboxService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Agent 运行器（Runner）—— 代理并调度 AgentHandler 的核心组件。
 *
 * <p>角色：Runner 是整个 Agent 引擎的运行时容器，充当外部请求与底层 Agent 框架之间的代理层。
 * 它将所有调用委托给注入的 {@link AgentHandler} 适配器，并在这一层添加了生命周期管理、
 * 健康检查、事件序列号分配和流式查询编排等横切逻辑。</p>
 *
 * <p>职责：</p>
 * <ul>
 *   <li>将生命周期方法（init、start、stop、shutdown）委托给 AgentHandler 适配器</li>
 *   <li>处理流式查询（streamQuery），编排完整的响应流程：created -> in_progress -> 流式内容 -> completed</li>
 *   <li>管理健康状态，对外提供 {@link #isHealthy()} 健康检查接口</li>
 *   <li>为每个事件分配递增的序列号（sequence number），保证客户端收到的事件顺序有序</li>
 *   <li>在流式查询出错时，捕获异常并生成标准化的失败响应</li>
 *   <li>提取并汇总 Token 使用量（usage）信息</li>
 * </ul>
 *
 * <p>设计模式：代理模式（Proxy Pattern）—— Runner 在真实 AgentHandler 之上增加了
 * 生命周期管理、事件序列化和错误处理等附加逻辑。</p>
 *
 * <p>线程安全说明：health 字段使用 volatile 保证可见性；sequenceGenerator 使用 AtomicInteger
 * 保证序列号的原子递增。单个 Runner 实例可被多个线程并发调用 streamQuery。</p>
 */
public class Runner {
    /** 日志记录器 */
    private static final Logger logger = LoggerFactory.getLogger(Runner.class);

    /** 被代理的 Agent 适配器，封装了特定框架（如 AgentScope）的 Agent 逻辑 */
    private final AgentHandler adapter;

    /** 运行器健康标志，volatile 保证多线程可见性 */
    private volatile boolean health = false;

    /** 事件序列号生成器，原子递增，保证事件顺序的唯一标识 */
    private final AtomicInteger sequenceGenerator = new AtomicInteger(0);
    
    /**
     * 构造函数，注入 Agent 适配器。
     *
     * @param adapter 被代理的 AgentHandler 实例，不能为 null
     * @throws IllegalArgumentException 如果 adapter 为 null
     */
    public Runner(AgentHandler adapter) {
        if (adapter == null) {
            throw new IllegalArgumentException("AgentAdapter cannot be null");
        }
        this.adapter = adapter;
    }

    /**
     * 初始化运行器。当前为空实现，预留扩展点。
     * 子类可在需要时覆盖此方法完成初始化工作。
     */
    public void init() {

    }
    
    /**
     * 启动运行器，委托给适配器的 start 方法。
     *
     * <p>启动后，运行器健康状态置为 true，开始可以接收并处理查询请求。</p>
     */
    public void start() {
        adapter.start();       // 委托给底层适配器启动 Agent
        this.health = true;    // 标记运行器为健康状态
        logger.info("[Runner] Runner started successfully");
    }

    /**
     * 停止运行器，委托给适配器的 stop 方法。
     * 停止后运行器不再处理新的查询请求。
     */
    public void stop() {
        adapter.stop();
        this.health = false;
        logger.info("[Runner] Runner stopped");
    }

    /**
     * 关闭运行器，释放所有资源。
     * 与 stop 不同，shutdown 是彻底终止，运行器此后不可再使用。
     */
    public void shutdown() {
        this.health = false;
        logger.info("[Runner] Runner shutdown completed");
    }
    
    /**
     * 流式查询 —— Runner 的核心方法，编排完整的流式响应生命周期。
     *
     * <p>该方法接收 Agent 请求，通过适配器层完成消息格式转换、流式调用和事件适配，
     * 最终返回一个带有序列号的事件流（Flux&lt;Event&gt;）。</p>
     *
     * <p>处理流程：</p>
     * <ol>
     *   <li>检查运行器健康状态，不健康则抛出异常</li>
     *   <li>确保请求中包含 sessionId 和 userId（不存在则自动生成）</li>
     *   <li>发射 "created" 状态的初始响应事件</li>
     *   <li>发射 "in_progress" 状态事件</li>
     *   <li>使用 MessageAdapter 将运行时消息转换为框架特定消息格式</li>
     *   <li>委托给适配器的 streamQuery 获取原始框架流</li>
     *   <li>使用 StreamAdapter 将框架流适配为运行时 Event 流</li>
     *   <li>为每个事件分配递增序列号</li>
     *   <li>收集已完成的消息到响应对象中</li>
     *   <li>错误时生成标准化失败响应</li>
     *   <li>成功完成后提取 Token 用量并发射 "completed" 事件</li>
     * </ol>
     *
     * @param request Agent 请求对象，包含输入消息和会话信息
     * @return 响应式事件流（Flux），包含 created/in_progress/内容/completed 等事件
     * @throws IllegalStateException 如果运行器尚未启动
     * @throws RuntimeException 如果 StreamAdapter 不可用
     */
    public Flux<Event> streamQuery(AgentRequest request) {
        // 获取适配器声明的框架类型（如 "agentscope"）
        String frameworkType = adapter.getFrameworkType();

        // 健康检查 —— 运行器必须已启动
        if (!health) {
            throw new IllegalStateException(
                "Runner has not been started. Please call 'runner.start()' or use 'async with Runner()' before calling 'streamQuery'."
            );
        }

        // 确保 sessionId 和 userId 已设置，未设置时自动生成
        if (request.getSessionId() == null || request.getSessionId().isEmpty()) {
            request.setSessionId(UUID.randomUUID().toString());
        }
        if (request.getUserId() == null || request.getUserId().isEmpty()) {
            request.setUserId(request.getSessionId());
        }

        // 创建响应对象，绑定请求 ID
        AgentResponse response = new AgentResponse(request.getId());
        response.setSessionId(request.getSessionId());

        // 发射初始 "created" 状态事件
        Flux<Event> initialResponse = Flux.just(
            withSequenceNumber(response.created())
        );

        // 发射 "in_progress" 状态事件
        Flux<Event> inProgressResponse = Flux.just(
            withSequenceNumber(response.inProgress())
        );

        // 错误追踪标志，用于判断流处理过程中是否发生过错误
        AtomicBoolean hasError = new AtomicBoolean(false);

        // 获取消息适配器和流适配器（根据框架类型选择）
        MessageAdapter messageAdapter = adapter.getMessageAdapter();
        StreamAdapter streamAdapter = adapter.getStreamAdapter();

        if (streamAdapter == null) {
            throw new RuntimeException(
                String.format("StreamAdapter is not available for framework type '%s'", frameworkType)
            );
        }

        // 将运行时消息格式转换为框架特定的消息格式
        Object frameworkMessages = messageAdapter.messageToFrameworkMsg(request.getInput());

        // 委托适配器执行流式查询，获取原始框架流
        Flux<Object> rawFrameworkStream = adapter.streamQuery(request, frameworkMessages)
            .cast(Object.class); // 统一转为 Object 类型以处理不同框架的流类型

        // 使用 StreamAdapter 将原始框架流适配为运行时 Event 流
        Flux<Event> eventStream = streamAdapter.adaptFrameworkStream(rawFrameworkStream);

        // 为每个事件添加序列号，并收集已完成的消息
        Flux<Event> adapterStream = eventStream
            .map(this::withSequenceNumber)
            .doOnNext(event -> {
                // 收集已完成的消息到响应对象
                if (event instanceof Message message) {
                    if (RunStatus.COMPLETED.equals(message.getStatus())
                        && "message".equals(message.getObject())) {
                        response.addNewMessage(message);
                    }
                }
            })
            .onErrorResume(throwable -> {
                // 错误处理：标记错误并生成失败响应
                hasError.set(true);
                logger.error("Error happens in `query_handler`: {}", throwable.getMessage(), throwable);
                Error error = new Error();
                error.setCode("500");
                error.setMessage("Error happens in `query_handler`: " + throwable.getMessage());
                return Flux.just(withSequenceNumber(response.failed(error)));
            });

        // 组合所有流：初始事件 + 进行中 + 适配器流，然后追加完成事件
        return Flux.concat(
            initialResponse,
            inProgressResponse,
            adapterStream
        ).concatWith(
            // 延迟执行：仅在流正常完成时才生成 completed 事件
            Flux.defer(() -> {
                // 如果发生了错误，不再追加 completed 事件
                if (hasError.get() || RunStatus.FAILED.equals(response.getStatus())) {
                    return Flux.empty();
                }

                // 从最后一条消息中提取 Token 使用量
                try {
                    if (response.getOutput() != null && !response.getOutput().isEmpty()) {
                        Message lastMessage = response.getOutput().get(response.getOutput().size() - 1);
                        if (lastMessage.getUsage() != null) {
                            response.setUsage(lastMessage.getUsage());
                        }
                    }
                } catch (IndexOutOfBoundsException e) {
                    // 空消息列表时的安全处理
                    logger.debug("Could not extract usage: {}", e.getMessage());
                }

                // 发射最终的 "completed" 事件
                return Flux.just(withSequenceNumber(response.completed()));
            })
        );
    }

    /**
     * 为事件分配递增的序列号。
     * 序列号用于客户端对事件的排序和去重。
     *
     * @param event 需要分配序列号的事件
     * @return 带有序列号的同一事件对象
     */
    private Event withSequenceNumber(Event event) {
        event.setSequenceNumber(sequenceGenerator.incrementAndGet());
        return event;
    }

    /**
     * 检查运行器是否健康。
     * 健康条件：运行器自身已启动（health=true）且底层适配器也健康。
     *
     * @return true 表示健康，false 表示不健康
     */
    public boolean isHealthy() {
        return health && adapter.isHealthy();
    }

    /**
     * 获取被代理的 Agent 适配器实例。
     *
     * @return AgentHandler 适配器实例
     */
    public AgentHandler getAgent() {
        return adapter;
    }

    /**
     * 获取适配器关联的沙箱服务。
     * 沙箱服务用于在隔离环境中执行工具调用（如代码执行、文件操作等）。
     *
     * @return 沙箱服务实例
     */
    public SandboxService getSandboxService() {
        return adapter.getSandboxService();
    }

}

