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
 * 文件名称: BuiltInSandboxProvider.java
 * 模块: sandbox-core
 * 包: io.agentscope.runtime.sandbox.manager.registry
 *
 * 内置沙箱提供者，提供系统预置的沙箱类型注册。
 */

package io.agentscope.runtime.sandbox.manager.registry;

import io.agentscope.runtime.sandbox.box.*;

import java.util.Collection;
import java.util.List;

public class BuiltInSandboxProvider implements SandboxProvider {

    @Override
    public Collection<Class<?>> getSandboxClasses() {
        return List.of(
                BaseSandbox.class,
                FilesystemSandbox.class,
                BrowserSandbox.class,
                APPWorldSandbox.class,
                BFCLSandbox.class,
                GuiSandbox.class,
                MobileSandbox.class,
                WebShopSandbox.class
        );
    }
}

