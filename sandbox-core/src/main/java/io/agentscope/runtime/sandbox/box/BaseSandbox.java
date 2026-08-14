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
 * 文件名称: BaseSandbox.java
 * 模块: sandbox-core
 * 包: io.agentscope.runtime.sandbox.box
 *
 * 基础沙箱实现，提供 Python 代码执行和 Shell 命令运行的基础能力。
 * 通过 @RegisterSandbox 注解注册到沙箱注册表中，使用默认基础镜像。
 * 其他特殊用途的沙箱（如 BrowserSandbox、GuiSandbox 等）可继承此类。
 */

package io.agentscope.runtime.sandbox.box;

import io.agentscope.runtime.sandbox.manager.SandboxService;
import io.agentscope.runtime.sandbox.manager.fs.FileSystemConfig;
import io.agentscope.runtime.sandbox.manager.fs.local.LocalFileSystemConfig;
import io.agentscope.runtime.sandbox.manager.registry.RegisterSandbox;

import java.util.HashMap;
import java.util.Map;

/**
 * 基础沙箱 —— 提供代码执行和命令运行能力的默认沙箱类型。
 *
 * <p>角色：作为最基础的沙箱实现，提供 IPython 代码执行和 Shell 命令运行功能。
 * 其他专用沙箱（浏览器沙箱、GUI 沙箱等）可继承此类并扩展特定能力。</p>
 *
 * <p>设计模式：继承扩展模式 —— 通过子类化来扩展沙箱功能；
 * 注解驱动配置 —— 使用 @RegisterSandbox 注解声明沙箱元数据。</p>
 */
@RegisterSandbox(
        imageName = "agentscope-registry.ap-southeast-1.cr.aliyuncs.com/agentscope/runtime-sandbox-base:latest",
        sandboxType = "base",
        securityLevel = "medium",
        timeout = 30,
        description = "Base Sandbox"
)
public class BaseSandbox extends Sandbox {

    public BaseSandbox(
            SandboxService managerApi,
            String userId,
            String sessionId
    ) {
        this(managerApi, userId, sessionId, Map.of());
    }

    public BaseSandbox(
            SandboxService managerApi,
            String userId,
            String sessionId,
            FileSystemConfig fileSystemConfig
    ) {
        this(managerApi, userId, sessionId, fileSystemConfig, Map.of());
    }

    public BaseSandbox(
            SandboxService managerApi,
            String userId,
            String sessionId,
            Map<String, String> environment
    ) {
        this(managerApi, userId, sessionId, LocalFileSystemConfig.builder().build(), environment);
    }

    public BaseSandbox(
            SandboxService managerApi,
            String userId,
            String sessionId,
            FileSystemConfig fileSystemConfig,
            Map<String, String> environment
    ) {
        super(managerApi, userId, sessionId, "base", fileSystemConfig, environment);
    }

    /**
     * 执行 IPython 代码。
     * 通过沙箱工具调用机制在隔离环境中执行 Python 代码。
     *
     * @param code 要执行的 Python 代码
     * @return 执行结果（JSON 字符串）
     */
    public String runIpythonCell(String code) {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("code", code);
        return callTool("run_ipython_cell", arguments);
    }

    /**
     * 执行 Shell 命令。
     * 通过沙箱工具调用机制在隔离环境中执行 Shell 命令。
     *
     * @param command 要执行的 Shell 命令
     * @return 执行结果（JSON 字符串）
     */
    public String runShellCommand(String command) {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("command", command);
        return callTool("run_shell_command", arguments);
    }
}

