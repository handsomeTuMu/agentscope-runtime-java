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
 * 文件名称: HostPrerequisiteError.java
 * 模块: sandbox-core
 * 包: io.agentscope.runtime.sandbox.box.model
 *
 * 宿主机前置条件错误模型，表示沙箱启动前的宿主机环境检查失败信息。
 */

package io.agentscope.runtime.sandbox.box.model;

/**
 * Custom exception raised when host prerequisites
 * for MobileSandbox are not met.
 */
public class HostPrerequisiteError extends RuntimeException {
    public HostPrerequisiteError(String message) {
        super(message);
    }
}

