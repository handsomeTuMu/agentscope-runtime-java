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
 * 文件名称: SandboxKey.java
 * 模块: sandbox-core
 * 包: io.agentscope.runtime.sandbox.manager.model.container
 *
 * 沙箱键模型，用于在 SandboxMap 中唯一标识一个沙箱实例。
 */

package io.agentscope.runtime.sandbox.manager.model.container;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record SandboxKey(String userID, String sessionID, String sandboxType) {
    private static final Logger logger = LoggerFactory.getLogger(SandboxKey.class);

    @Override
    public String toString() {
        return "SandboxKey{" + "userID='" + userID + '\'' + ", sessionID='" + sessionID + '\'' + ", sandboxType=" + sandboxType + '}';
    }
}
