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
 * 文件名称: AgentScopeAgentHandler.java
 * 模块: engine-core
 * 包: io.agentscope.runtime.adapters.agentscope
 *
 * AgentScopeAgentHandler。
 */

package io.agentscope.runtime.adapters.agentscope;

import io.agentscope.runtime.adapters.AgentHandler;
import io.agentscope.runtime.adapters.MessageAdapter;
import io.agentscope.runtime.adapters.StreamAdapter;
import io.agentscope.runtime.engine.schemas.AgentRequest;
import io.agentscope.runtime.engine.services.agent_state.StateService;
import io.agentscope.runtime.engine.services.memory.service.MemoryService;
import io.agentscope.runtime.engine.services.memory.service.SessionHistoryService;
import io.agentscope.runtime.sandbox.manager.SandboxService;
import reactor.core.publisher.Flux;

/**
 * AgentScope 框架适配器实现的抽象基类。
 *
 * <p>该类为 AgentScope 适配器提供通用功能，包括：</p>
 * <ul>
 *   <li>框架类型标识（"agentscope"）</li>
 *   <li>用于转换 AgentScope 流式事件的流适配器</li>
 * </ul>
 *
 * <p>架构位置：该类是 AgentScope 框架与运行时之间的适配器基类，
 * 持有状态服务、会话历史服务和记忆服务的引用，管理这些服务的生命周期。</p>
 *
 * <p>子类必须实现以下生命周期方法：</p>
 * <ul>
 *   <li>{@link #start()} - 启动适配器</li>
 *   <li>{@link #streamQuery(AgentRequest, Object)} - 处理查询</li>
 *   <li>{@link #stop()} - 停止适配器</li>
 *   <li>{@link #isHealthy()} - 检查健康状态</li>
 * </ul>
 */
public abstract class AgentScopeAgentHandler implements AgentHandler {

    /** 流适配器，用于将 AgentScope 流式事件转换为运行时 Event 流 */
    protected final StreamAdapter streamAdapter;
    /** 消息适配器，用于在 AgentScope Msg 和运行时 Message 之间转换 */
    protected final MessageAdapter messageAdapter;
    /** 沙箱服务，用于隔离执行文件系统和进程操作 */
    protected SandboxService sandboxService;

    /** 短期状态服务（来自运行时），专门用于 AgentScope 的状态管理 */
    protected StateService stateService;
    /** 短期会话历史服务（来自运行时） */
    protected SessionHistoryService sessionHistoryService;
    /** 长期记忆服务（来自运行时） */
    protected MemoryService memoryService;

    /**
     * 创建 AgentScopeAgentHandler 实例，注入所有必要的服务依赖。
     *
     * @param stateService 状态服务实例
     * @param sessionHistoryService 会话历史服务实例
     * @param memoryService 记忆服务实例
     * @param sandboxService 沙箱服务实例
     */
    AgentScopeAgentHandler(StateService stateService, SessionHistoryService sessionHistoryService, MemoryService memoryService, SandboxService sandboxService) {
        this();
        this.stateService = stateService;
        this.sessionHistoryService = sessionHistoryService;
        this.memoryService = memoryService;
        this.sandboxService = sandboxService;
    }

    /**
     * 创建新的 AgentScopeAgentAdapter 实例。
     * 初始化 AgentScope 框架的流适配器和消息适配器。
     */
    protected AgentScopeAgentHandler() {
        this.streamAdapter = new AgentScopeStreamAdapter();
        this.messageAdapter = new AgentScopeMessageAdapter();
    }

    /**
     * 设置会话历史服务。
     *
     * @param sessionHistoryService 会话历史服务实例
     */
    public void setSessionHistoryService(SessionHistoryService sessionHistoryService) {
        this.sessionHistoryService = sessionHistoryService;
    }

    /**
     * 设置状态服务。
     *
     * @param stateService 状态服务实例
     */
    public void setStateService(StateService stateService) {
        this.stateService = stateService;
    }

    /**
     * 设置长期记忆服务。
     *
     * @param memoryService 记忆服务实例
     */
    public void setMemoryService(MemoryService memoryService) {
        this.memoryService = memoryService;
    }

    /**
     * 获取该适配器支持的框架类型。
     *
     * @return "agentscope" 作为框架类型
     */
    @Override
    public String getFrameworkType() {
        return "agentscope";
    }

    /**
     * 获取 AgentScope 框架的 StreamAdapter 实现。
     *
     * @return AgentScopeStreamAdapter 实例
     */
    @Override
    public StreamAdapter getStreamAdapter() {
        return streamAdapter;
    }

    /**
     * 获取 AgentScope 框架的 MessageAdapter 实现。
     *
     * @return AgentScopeMessageAdapter 实例
     */
    @Override
    public MessageAdapter getMessageAdapter() {
        return messageAdapter;
    }

    // 生命周期方法 - 必须由子类实现

    /**
     * 启动适配器并标记为就绪状态，准备处理查询。
     * 启动状态服务和会话历史服务（如果存在）。
     */
    @Override
    public void start() {
        if (stateService != null) {
            stateService.start();  // 启动状态服务
        }
        if (sessionHistoryService != null) {
            sessionHistoryService.start();  // 启动会话历史服务
        }
    }

    /**
     * 停止适配器，阻止其接受新查询。
     * 按顺序停止状态服务、会话历史服务和沙箱服务。
     */
    @Override
    public void stop() {
        if (stateService != null) {
            stateService.stop();  // 停止状态服务
        }
        if (sessionHistoryService != null) {
            sessionHistoryService.stop();  // 停止会话历史服务
        }
        if(sandboxService != null){
            sandboxService.stop();  // 停止沙箱服务
        }
    }

    /**
     * 检查适配器是否处于健康/就绪状态。
     * 必须由子类实现。
     *
     * @return 如果适配器健康且就绪则返回 true，否则返回 false
     */
    @Override
    public abstract boolean isHealthy();

    /**
     * 处理 Agent 查询并返回框架特定事件的流。
     * 必须由子类实现。
     *
     * @param request 包含输入消息、会话信息等的 Agent 请求
     * @param messages 已转换的框架特定消息（如 AgentScope 的 List&lt;Msg&gt;），为 null 时表示无需转换
     * @return AgentScope Event 对象的 Flux 流（Flux&lt;io.agentscope.core.agent.Event&gt;）
     */
    @Override
    public abstract Flux<?> streamQuery(AgentRequest request, Object messages);

    /**
     * 获取沙箱服务实例。
     *
     * @return 沙箱服务
     */
    public SandboxService getSandboxService(){
        return sandboxService;
    }

    /**
     * 设置沙箱服务。
     *
     * @param sandboxService 沙箱服务实例
     */
    public void setSandboxService(SandboxService sandboxService){
        this.sandboxService = sandboxService;
    }

}

