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
 * 文件名称: ResponseApiController.java
 * 模块: web
 * 包: io.agentscope.runtime.protocol.responseapi.controller
 *
 * OpenAI Responses API 兼容的 REST 控制器。
 * 提供与 OpenAI Responses API 兼容的端点，支持流式（SSE）和非流式（JSON）两种响应模式。
 * 端点路径：/compatible-mode/v1/responses
 */

package io.agentscope.runtime.protocol.responseapi.controller;

import io.agentscope.runtime.engine.Runner;
import io.agentscope.runtime.protocol.ProtocolConfig;
import io.agentscope.runtime.protocol.responseapi.ResponseApiHandler;
import io.agentscope.runtime.protocol.responseapi.ResponseApiHandlerConfiguration;
import io.agentscope.runtime.protocol.responseapi.model.ResponseApiRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * OpenAI Responses API 兼容的 REST 控制器。
 *
 * <p>角色：作为 Response API 协议的 HTTP 入口，接收客户端请求并委托给
 * {@link ResponseApiHandler} 处理。根据请求中的 stream 参数自动选择流式或非流式响应。</p>
 *
 * <p>设计模式：前端控制器模式（Front Controller）—— 作为统一的请求入口，
 * 将请求处理委托给专门的处理器组件。</p>
 */
@RestController
@RequestMapping("/compatible-mode/v1")
public class ResponseApiController {

    private static final Logger logger = LoggerFactory.getLogger(ResponseApiController.class);

    /** Response API 处理器，负责将请求转换为 Runner 调用 */
    private final ResponseApiHandler responseApiHandler;

    /**
     * 构造函数，通过 Spring 依赖注入获取 Runner 和协议配置。
     *
     * @param runner Agent 运行器实例
     * @param protocolConfigs 协议配置提供者
     */
    public ResponseApiController(Runner runner, ObjectProvider<ProtocolConfig> protocolConfigs) {
        this.responseApiHandler = ResponseApiHandlerConfiguration.getInstance(runner, protocolConfigs).responseApiHandler();
    }

    /**
     * 流式聊天完成端点（兼容 OpenAI Responses API）。
     *
     * <p>根据请求中的 stream 参数决定返回流式（SSE）还是非流式（JSON）响应：
     * <ul>
     *   <li>stream=true：返回 Server-Sent Events 流式响应</li>
     *   <li>stream=false：返回聚合后的 JSON 响应</li>
     * </ul></p>
     *
     * @param request Response API 请求体
     * @return 流式响应（Flux&lt;ServerSentEvent&gt;）或 JSON 响应对象
     */
    @PostMapping(value = "/responses",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_EVENT_STREAM_VALUE})
    @ResponseBody
    public Object streamChatCompletions(@RequestBody ResponseApiRequest request) {
        logger.info("Received OpenAI Responses API chat completion request");

        // 检查请求是否要求流式响应（Responses API 默认为流式）
        Boolean stream = request.getStream();

        if (Boolean.TRUE.equals(stream)) {
            // 流式模式：返回 SSE 事件流
            return responseApiHandler.handleStreamingResponse(request);
        } else {
            // 非流式模式：返回聚合的 JSON 响应
            logger.info("Non-streaming request received, returning aggregated response");
            return responseApiHandler.handleNonStreamingResponse(request);
        }
    }
}
