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
 * 文件名称: Sandbox.java
 * 模块: sandbox-core
 * 包: io.agentscope.runtime.sandbox.box
 *
 * 沙箱实例核心类，代表一个隔离的代码执行环境。
 * 沙箱可以是 Docker 容器、AgentRun 实例、函数计算（FC）实例等，
 * Agent 在沙箱中安全地执行 Python 代码、Shell 命令、文件操作等。
 * 实现了 AutoCloseable 接口，支持 try-with-resources 自动资源释放。
 */


package io.agentscope.runtime.sandbox.box;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.agentscope.runtime.sandbox.manager.SandboxService;
import io.agentscope.runtime.sandbox.manager.fs.FileSystemConfig;
import io.agentscope.runtime.sandbox.manager.fs.local.LocalFileSystemConfig;
import io.agentscope.runtime.sandbox.manager.model.container.ContainerModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 沙箱实例 —— 隔离的代码执行环境。
 *
 * <p>角色：代表一个隔离的运行环境（如 Docker 容器），Agent 在其中安全地
 * 执行代码、运行命令、操作文件系统。每个沙箱实例关联到一个用户会话，
 * 并通过 {@link SandboxService} 管理底层容器的生命周期。</p>
 *
 * <p>职责：</p>
 * <ul>
 *   <li>管理沙箱 ID、用户 ID、会话 ID、沙箱类型等元数据</li>
 *   <li>延迟初始化：首次使用时自动创建底层容器</li>
 *   <li>通过 {@link SandboxService} 代理调用工具（listTools、callTool 等）</li>
 *   <li>支持 MCP 服务器配置添加</li>
 *   <li>资源自动释放（AutoCloseable）</li>
 * </ul>
 *
 * <p>设计模式：代理模式 —— Sandbox 将工具调用请求委托给 SandboxService，
 * 后者再委托给具体的沙箱客户端（SandboxClient）。</p>
 */
public class Sandbox implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(Sandbox.class);

    /** 沙箱服务管理器 API，负责底层容器的创建、管理和调用 */
    protected SandboxService managerApi;

    /** 沙箱唯一标识符（即底层容器 ID） */
    protected String sandboxId;

    /** 用户 ID */
    protected String userId;

    /** 会话 ID */
    protected String sessionId;

    /** 沙箱类型（如 "base"、"browser"、"filesystem" 等） */
    protected String sandboxType;

    /** 沙箱是否已关闭标志 */
    protected boolean closed = false;

    /** 环境变量映射 */
    protected Map<String, String> environment;

    /** 文件系统配置 */
    protected FileSystemConfig fileSystemConfig;

    /**
     * JSON 反序列化构造函数，用于从 JSON 数据重建沙箱实例。
     *
     * @param sandboxId 沙箱 ID
     * @param userId 用户 ID
     * @param sessionId 会话 ID
     * @param sandboxType 沙箱类型
     * @param fileSystemConfig 文件系统配置
     * @param environment 环境变量
     * @param closed 是否已关闭
     */
    @JsonCreator
    public Sandbox(
            @JsonProperty("sandboxId") String sandboxId,
            @JsonProperty("userId") String userId,
            @JsonProperty("sessionId") String sessionId,
            @JsonProperty("sandboxType") String sandboxType,
            @JsonProperty("fileSystemConfig") FileSystemConfig fileSystemConfig,
            @JsonProperty("environment") Map<String, String> environment,
            @JsonProperty("closed") boolean closed
    ) {
        this.sandboxId = sandboxId;
        this.userId = userId;
        this.sessionId = sessionId;
        this.sandboxType = sandboxType;
        this.fileSystemConfig = fileSystemConfig;
        this.environment = environment != null ? new HashMap<>(environment) : new HashMap<>();
        this.closed = closed;
    }

    public Sandbox(SandboxService managerApi,
                   String userId,
                   String sessionId,
                   String sandboxType
    ) {
        this(managerApi, userId, sessionId, sandboxType, LocalFileSystemConfig.builder().build(), Map.of());
    }

    public Sandbox(SandboxService managerApi,
                   String userId,
                   String sessionId,
                   String sandboxType,
                   FileSystemConfig fileSystemConfig
    ) {
        this(managerApi, userId, sessionId, sandboxType, fileSystemConfig, Map.of());
    }

    public Sandbox(
            SandboxService managerApi,
            String userId,
            String sessionId,
            String sandboxType,
            Map<String, String> environment
    ) {
        this(managerApi, userId, sessionId, sandboxType, LocalFileSystemConfig.builder().build(), environment);
    }

    public Sandbox(
            SandboxService managerApi,
            String userId,
            String sessionId,
            String sandboxType,
            FileSystemConfig fileSystemConfig,
            Map<String, String> environment
    ) {
        this.managerApi = managerApi;
        this.userId = userId;
        this.sessionId = sessionId;
        this.sandboxType = sandboxType;
        this.fileSystemConfig = fileSystemConfig;
        this.environment = new HashMap<>(environment);
    }

    public void setSandboxId(String sandboxId) {
        this.sandboxId = sandboxId;
    }

    public String getSandboxId() {
        return sandboxId;
    }

    public String getUserId() {
        return userId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getSandboxType() {
        return sandboxType;
    }

    public Map<String, String> getEnvironment() {
        return environment;
    }

    public FileSystemConfig getFileSystemConfig() {
        return fileSystemConfig;
    }

    /**
     * 延迟初始化沙箱。
     * 如果沙箱 ID 为空，则通过 SandboxService 创建底层容器并获取容器 ID。
     * 这是一个懒加载机制，只在首次使用时才创建容器。
     */
    private void initializeSandbox(){
        if (sandboxId == null || sandboxId.isEmpty()) {
            try {
                // 通过管理器 API 创建底层容器
                ContainerModel containerModel = managerApi.createContainer(this);
                if (containerModel == null) {
                    throw new RuntimeException(
                            "No sandbox available. Please check if sandbox images exist."
                    );
                }
                this.sandboxId = containerModel.getContainerId();
                logger.info("Sandbox initialized: {} (type={}, user={}, session={})", this.sandboxId, sandboxType, userId, sessionId);
            } catch (Exception e) {
                logger.error("Failed to initialize sandbox: {}", e.getMessage());
                throw new RuntimeException("Failed to initialize sandbox", e);
            }
        }
    }

    /**
     * 获取沙箱信息（容器模型）。
     * 如果沙箱尚未初始化，会先执行延迟初始化。
     *
     * @return 容器模型信息
     */
    @JsonIgnore
    public ContainerModel getInfo() {
        initializeSandbox();
        try {
            return managerApi.getInfo(this);
        }
        catch (Exception e) {
            logger.error("Failed to get sandbox info: {}", e.getMessage());
            throw new RuntimeException("Failed to get sandbox info", e);
        }
    }

    /**
     * 列出沙箱中所有可用的工具。
     *
     * @return 工具名称到工具信息的映射
     */
    public Map<String, Object> listTools() {
        return listTools(null);
    }

    /**
     * 列出沙箱中指定类型的工具。
     *
     * @param toolType 工具类型过滤，为 null 时列出所有工具
     * @return 工具名称到工具信息的映射
     */
    public Map<String, Object> listTools(String toolType) {
        initializeSandbox();
        try{
            return managerApi.listTools(this, toolType);
        }
        catch (Exception e) {
            logger.error("Failed to list tools: {}", e.getMessage());
            throw new RuntimeException("Failed to list tools", e);
        }
    }

    /**
     * 调用沙箱中的工具。
     *
     * @param name 工具名称
     * @param arguments 工具参数映射
     * @return 工具执行结果（JSON 字符串）
     */
    public String callTool(String name, Map<String, Object> arguments) {
        initializeSandbox();
        try{
            return managerApi.callTool(this, name, arguments);
        }
        catch (Exception e) {
            logger.error("Failed to call tool {}: {}", name, e.getMessage());
            throw new RuntimeException("Failed to call tool " + name, e);
        }
    }

    /**
     * 向沙箱添加 MCP 服务器配置（不覆盖已有配置）。
     *
     * @param serverConfigs MCP 服务器配置映射
     * @return 添加结果
     */
    public Map<String, Object> addMcpServers(Map<String, Object> serverConfigs) {
        return addMcpServers(serverConfigs, false);
    }

    /**
     * 向沙箱添加 MCP 服务器配置。
     *
     * @param serverConfigs MCP 服务器配置映射
     * @param overwrite 是否覆盖已有的同名服务器配置
     * @return 添加结果
     */
    public Map<String, Object> addMcpServers(Map<String, Object> serverConfigs, boolean overwrite) {
        initializeSandbox();
        try{
            return managerApi.addMcpServers(this, serverConfigs, overwrite);
        }
        catch (Exception e) {
            logger.error("Failed to add MCP servers: {}", e.getMessage());
            throw new RuntimeException("Failed to add MCP servers", e);
        }
    }

    /**
     * 关闭并释放沙箱资源。
     * 通过 SandboxService 停止并移除底层容器。
     * 支持 AutoCloseable，可使用 try-with-resources 自动调用。
     */
    @Override
    public void close() {
        if (closed || sandboxId == null || sandboxId.isEmpty()) {
            return;
        }

        closed = true;

        try {
            logger.info("Auto-releasing sandbox: {}", sandboxId);
            if (!managerApi.stopAndRemoveSandbox(sandboxId)) {
                logger.warn("Sandbox {} failed to remove", sandboxId);
            }
        } catch (Exception e) {
            logger.error("Failed to cleanup sandbox: {}", e.getMessage());
        }
    }

    /**
     * 手动释放沙箱资源。
     * 等同于调用 {@link #close()}，强制释放底层容器。
     */
    public void release() {
        close();
    }

    /** @return 沙箱是否已关闭 */
    public boolean isClosed() {
        return closed;
    }
}
