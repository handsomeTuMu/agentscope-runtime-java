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
 * 文件名称: ResponseBuilder.java
 * 模块: engine-core
 * 包: io.agentscope.runtime.engine.helpers
 *
 * 响应构建器，负责构建和管理 AgentResponse 对象。
 * 使用建造者模式（Builder Pattern）分层次地构建 Agent 响应，包含三个嵌套构建器：
 * ResponseBuilder（响应级） -> MessageBuilder（消息级） -> ContentBuilder（内容级）。
 * 支持流式输出的增量构建，包括文本分片、数据增量合并等高级功能。
 */

package io.agentscope.runtime.engine.helpers;

import io.agentscope.runtime.engine.schemas.*;
import java.util.*;
import java.util.stream.Stream;

/**
 * 响应构建器（Response Builder）—— AgentResponse 的核心构建组件。
 *
 * <p>角色：负责构建和管理 Agent 响应对象的整个生命周期，协调 MessageBuilder 工作。
 * 这是三层嵌套建造者结构的最外层。</p>
 *
 * <p>职责：</p>
 * <ul>
 *   <li>创建和管理 AgentResponse 对象</li>
 *   <li>管理响应状态转换（created -> in_progress -> completed / failed）</li>
 *   <li>创建 MessageBuilder 实例来构建消息</li>
 *   <li>维护消息列表，支持按 ID 更新已有消息</li>
 *   <li>生成完整的流式响应序列</li>
 * </ul>
 *
 * <p>设计模式：建造者模式（Builder Pattern）—— 将复杂响应对象的构建过程封装起来，
 * 支持分步骤构建和流式增量输出。</p>
 */
public class ResponseBuilder {
    /** 会话 ID，标识当前对话会话 */
    private String sessionId;
    /** 响应 ID，唯一标识当前响应 */
    private String responseId;
    /** 响应创建时间戳（Unix 时间，秒） */
    private Long createdAt;
    /** 消息构建器列表 */
    private List<MessageBuilder> messageBuilders;
    /** 底层响应对象 */
    private AgentResponse response;

    /**
     * 默认构造函数，创建不带会话 ID 和响应 ID 的构建器。
     */
    public ResponseBuilder() {
        this(null, null);
    }

    /**
     * 带会话 ID 的构造函数。
     *
     * @param sessionId 会话 ID
     */
    public ResponseBuilder(String sessionId) {
        this(sessionId, null);
    }

    /**
     * 带会话 ID 和响应 ID 的完整构造函数。
     *
     * @param sessionId 会话 ID
     * @param responseId 响应 ID，为 null 时由调用方在后续设置
     */
    public ResponseBuilder(String sessionId, String responseId) {
        this.sessionId = sessionId;
        this.responseId = responseId;
        this.createdAt = System.currentTimeMillis() / 1000; // Unix 时间戳（秒）
        this.messageBuilders = new ArrayList<>();

        // 创建底层响应对象
        this.response = new AgentResponse(
            this.responseId,
            this.sessionId,
            this.createdAt
        );
        this.response.setOutput(new ArrayList<>());
    }

    /**
     * 重置构建器状态，生成新的响应 ID 和对象实例。
     * 用于复用同一个构建器实例来构建多个响应。
     */
    public void reset() {
        this.responseId = "response_" + UUID.randomUUID().toString();
        this.createdAt = System.currentTimeMillis() / 1000;
        this.messageBuilders = new ArrayList<>();

        // 重新创建响应对象
        this.response = new AgentResponse(
            this.responseId,
            this.sessionId,
            this.createdAt
        );
        this.response.setOutput(new ArrayList<>());
    }

    /**
     * 获取当前响应对象的引用。
     *
     * @return 当前构建的 AgentResponse 实例
     */
    public AgentResponse getResponseData() {
        return response;
    }

    /**
     * 将响应状态设置为 "created"（已创建）。
     *
     * @return 更新后的响应对象
     */
    public AgentResponse created() {
        response.created();
        return response;
    }

    /**
     * 将响应状态设置为 "in_progress"（处理中）。
     *
     * @return 更新后的响应对象
     */
    public AgentResponse inProgress() {
        response.inProgress();
        return response;
    }

    /**
     * 将响应状态设置为 "completed"（已完成）。
     *
     * @return 更新后的响应对象
     */
    public AgentResponse completed() {
        response.completed();
        return response;
    }

    /**
     * 创建消息构建器（MessageBuilder）。
     *
     * @param role 消息角色（如 "assistant"、"user"），为 null 时默认 "assistant"
     * @param messageType 消息类型（如 "message"），为 null 时默认 "message"
     * @return 新创建的 MessageBuilder 实例
     */
    public MessageBuilder createMessageBuilder(String role, String messageType) {
        if (role == null) {
            role = Role.ASSISTANT;
        }
        if (messageType == null) {
            messageType = MessageType.MESSAGE;
        }

        MessageBuilder messageBuilder = new MessageBuilder(this, role, messageType);
        this.messageBuilders.add(messageBuilder);
        return messageBuilder;
    }

    /**
     * 将消息添加到响应的输出列表中。
     * 如果已存在相同 ID 的消息，则替换之；否则追加到列表末尾。
     *
     * @param message 要添加的消息对象
     */
    public void addMessage(Message message) {
        if (response.getOutput() == null) {
            response.setOutput(new ArrayList<>());
        }

        // 检查是否存在相同 ID 的消息，存在则替换
        List<Message> output = response.getOutput();
        for (int i = 0; i < output.size(); i++) {
            if (output.get(i).getId().equals(message.getId())) {
                output.set(i, message);
                return;
            }
        }

        output.add(message);
    }

    /**
     * 更新响应输出列表中的消息。
     * 根据 ID 查找并替换已有消息。
     *
     * @param message 包含更新内容的消息对象
     */
    public void updateMessage(Message message) {
        if (response.getOutput() == null) {
            return;
        }

        List<Message> output = response.getOutput();
        for (int i = 0; i < output.size(); i++) {
            if (output.get(i).getId().equals(message.getId())) {
                output.set(i, message);
                return;
            }
        }
    }

    /**
     * 生成完整的流式响应事件序列。
     *
     * <p>生成的序列包含以下阶段：</p>
     * <ol>
     *   <li>created —— 响应已创建</li>
     *   <li>in_progress —— 响应处理中</li>
     *   <li>message.in_progress —— 消息开始构建</li>
     *   <li>text delta x N —— 逐个文本分片</li>
     *   <li>content completed —— 内容构建完成</li>
     *   <li>message completed —— 消息构建完成</li>
     *   <li>completed —— 响应已完成</li>
     * </ol>
     *
     * @param textTokens 文本分片列表，将按顺序作为增量内容输出
     * @param role 消息角色，为 null 时默认 "assistant"
     * @return 事件流，按上述顺序生成
     */
    public Stream<Event> generateStreamingResponse(List<String> textTokens, String role) {
        if (role == null) {
            role = Role.ASSISTANT;
        }

        List<Event> events = new ArrayList<>();

        // 重置状态，生成新的响应对象
        reset();

        // 1. 创建响应（created）
        events.add(created());

        // 2. 开始处理（in_progress）
        events.add(inProgress());

        // 3. 创建消息构建器
        MessageBuilder messageBuilder = createMessageBuilder(role, MessageType.MESSAGE);
        events.add(messageBuilder.getMessageData());

        // 4. 创建内容构建器
        ContentBuilder contentBuilder = messageBuilder.createContentBuilder(ContentType.TEXT);

        // 5. 流式输出文本分片
        for (String token : textTokens) {
            events.add(contentBuilder.addTextDelta(token));
        }

        // 6. 完成内容构建
        events.add(contentBuilder.complete());

        // 7. 完成消息构建
        events.add(messageBuilder.complete());

        // 8. 完成响应
        events.add(completed());

        return events.stream();
    }

    /**
     * 消息构建器（Message Builder）—— 负责构建和管理单个 Message 对象。
     *
     * <p>角色：三层嵌套建造者结构的中间层，负责构建消息对象，
     * 并协调 ContentBuilder 工作来构建消息中的内容块。</p>
     *
     * <p>设计模式：建造者模式 —— 作为 ResponseBuilder 的内部类，
     * 可以直接访问外部类的方法来更新响应状态。</p>
     */
    public class MessageBuilder {
        /** 所属的响应构建器引用 */
        private final ResponseBuilder responseBuilder;
        /** 消息角色（如 "assistant"、"user"） */
        private final String role;
        /** 消息唯一 ID */
        private final String messageId;
        /** 内容构建器列表 */
        private final List<ContentBuilder> contentBuilders;
        /** 底层消息对象 */
        private final Message message;

        /**
         * 构造消息构建器，立即创建消息对象并添加到响应中。
         *
         * @param responseBuilder 所属的响应构建器
         * @param role 消息角色
         * @param messageType 消息类型
         */
        public MessageBuilder(ResponseBuilder responseBuilder, String role, String messageType) {
            this.responseBuilder = responseBuilder;
            this.role = role;
            this.messageId = "msg_" + UUID.randomUUID().toString();
            this.contentBuilders = new ArrayList<>();

            // 创建底层消息对象
            this.message = new Message(messageType, role);
            this.message.setId(messageId);
            this.message.inProgress(); // 初始状态设为 "in_progress"

            // 立即将消息添加到响应输出列表
            responseBuilder.addMessage(this.message);
        }

        /**
         * 创建内容构建器（ContentBuilder）。
         *
         * @param contentType 内容类型（"text"、"image"、"data"、"audio"），为 null 时默认 "text"
         * @return 新创建的 ContentBuilder 实例
         */
        public ContentBuilder createContentBuilder(String contentType) {
            if (contentType == null) {
                contentType = ContentType.TEXT;
            }

            int index = contentBuilders.size();
            ContentBuilder contentBuilder = new ContentBuilder(this, contentType, index);
            contentBuilders.add(contentBuilder);
            return contentBuilder;
        }

        /**
         * 将内容添加到消息的内容列表中。
         * 如果已存在相同索引的内容，则替换之。
         *
         * @param content 要添加的内容对象
         */
        public void addContent(Content content) {
            if (message.getContent() == null) {
                message.setContent(new ArrayList<>());
            }

            List<Content> messageContent = message.getContent();

            // 检查是否存在相同索引的内容，存在则替换
            Integer contentIndex = content.getIndex();
            if (contentIndex != null) {
                for (int i = 0; i < messageContent.size(); i++) {
                    Content existing = messageContent.get(i);
                    if (contentIndex.equals(existing.getIndex())) {
                        messageContent.set(i, content);
                        // 通知响应构建器更新
                        responseBuilder.updateMessage(message);
                        return;
                    }
                }
            }

            messageContent.add(content);
            // 通知响应构建器更新
            responseBuilder.updateMessage(message);
        }

        /**
         * 获取当前消息对象的引用。
         *
         * @return 底层 Message 实例
         */
        public Message getMessageData() {
            return message;
        }

        /**
         * 完成消息构建，将状态设置为 "completed"。
         *
         * @return 完成的消息对象
         */
        public Message complete() {
            message.completed();
            // 通知响应构建器更新
            responseBuilder.updateMessage(message);
            return message;
        }
    }

    /**
     * 内容构建器（Content Builder）—— 负责构建和管理单个 Content 对象。
     *
     * <p>角色：三层嵌套建造者结构的最内层，支持文本（Text）、图片（Image）、
     * 数据（Data）和音频（Audio）四种内容类型的构建。</p>
     *
     * <p>职责：</p>
     * <ul>
     *   <li>为文本类型提供增量分片（text delta）支持</li>
     *   <li>为数据类型提供增量合并（data delta）支持</li>
     *   <li>在完成时自动合并所有增量内容</li>
     * </ul>
     *
     * <p>设计模式：建造者模式 —— 作为 MessageBuilder 的内部类，
     * 可直接访问消息构建器和响应构建器的方法。</p>
     */
    public class ContentBuilder {
        /** 所属的消息构建器引用 */
        private final MessageBuilder messageBuilder;
        /** 内容类型（"text"、"image"、"data"、"audio"） */
        private final String contentType;
        /** 内容在消息中的索引位置 */
        private final int index;
        /** 底层内容对象 */
        private final Content content;
        /** 文本分片列表（仅文本类型使用） */
        private List<String> textTokens;
        /** 数据增量列表（仅数据类型使用） */
        private List<Map<String, Object>> dataDeltas;

        /**
         * 构造内容构建器，根据类型初始化对应的数据结构。
         *
         * @param messageBuilder 所属的消息构建器
         * @param contentType 内容类型
         * @param index 内容在消息中的索引位置
         * @throws IllegalArgumentException 如果内容类型不被支持
         */
        public ContentBuilder(MessageBuilder messageBuilder, String contentType, int index) {
            this.messageBuilder = messageBuilder;
            this.contentType = contentType;
            this.index = index;

            // 根据类型初始化对应的数据结构和内容对象
            if (ContentType.TEXT.equals(contentType)) {
                this.textTokens = new ArrayList<>();
                this.content = new TextContent();
            } else if (ContentType.IMAGE.equals(contentType)) {
                this.content = new ImageContent();
            } else if (ContentType.DATA.equals(contentType)) {
                this.dataDeltas = new ArrayList<>();
                this.content = new DataContent();
            } else if (ContentType.AUDIO.equals(contentType)) {
                this.content = new AudioContent();
            } else {
                throw new IllegalArgumentException("Unsupported content type: " + contentType);
            }

            this.content.setType(contentType);
            this.content.setIndex(index);
            this.content.setMsgId(messageBuilder.messageId);
            this.content.setDelta(false); // 初始状态为非增量
        }

        /**
         * 添加文本增量分片（仅适用于文本类型）。
         * 创建一个增量内容对象返回，同时累积分片以便在完成时合并。
         *
         * @param text 文本分片内容
         * @return 增量文本内容对象
         * @throws IllegalArgumentException 如果当前内容不是文本类型
         */
        public TextContent addTextDelta(String text) {
            if (!ContentType.TEXT.equals(contentType)) {
                throw new IllegalArgumentException("addTextDelta only supported for text content");
            }

            textTokens.add(text);

            // 创建增量内容对象返回
            TextContent deltaContent = new TextContent();
            deltaContent.setType(ContentType.TEXT);
            deltaContent.setIndex(index);
            deltaContent.setDelta(true); // 标记为增量内容
            deltaContent.setMsgId(messageBuilder.messageId);
            deltaContent.setText(text);
            deltaContent.inProgress();

            return deltaContent;
        }

        /**
         * 设置完整文本内容（仅适用于文本类型）。
         *
         * @param text 完整文本
         * @return 文本内容对象
         * @throws IllegalArgumentException 如果当前内容不是文本类型
         */
        public TextContent setText(String text) {
            if (!ContentType.TEXT.equals(contentType)) {
                throw new IllegalArgumentException("setText only supported for text content");
            }

            if (content instanceof TextContent) {
                ((TextContent) content).setText(text);
                content.inProgress();
            }
            return (TextContent) content;
        }

        /**
         * 设置图片 URL（仅适用于图片类型）。
         *
         * @param imageUrl 图片 URL
         * @return 图片内容对象
         * @throws IllegalArgumentException 如果当前内容不是图片类型
         */
        public ImageContent setImageUrl(String imageUrl) {
            if (!ContentType.IMAGE.equals(contentType)) {
                throw new IllegalArgumentException("setImageUrl only supported for image content");
            }

            if (content instanceof ImageContent) {
                ((ImageContent) content).setImageUrl(imageUrl);
                content.inProgress();
            }
            return (ImageContent) content;
        }

        /**
         * 设置数据内容（仅适用于数据类型）。
         *
         * @param data 数据映射
         * @return 数据内容对象
         * @throws IllegalArgumentException 如果当前内容不是数据类型
         */
        public DataContent setData(Map<String, Object> data) {
            if (!ContentType.DATA.equals(contentType)) {
                throw new IllegalArgumentException("setData only supported for data content");
            }

            if (content instanceof DataContent) {
                ((DataContent) content).setData(data);
                content.inProgress();
            }
            return (DataContent) content;
        }

        /**
         * 添加数据增量（仅适用于数据类型）。
         * 增量数据将在完成时与已有数据进行智能合并。
         *
         * @param deltaData 增量数据映射
         * @return 增量数据内容对象
         * @throws IllegalArgumentException 如果当前内容不是数据类型
         */
        public DataContent addDataDelta(Map<String, Object> deltaData) {
            if (!ContentType.DATA.equals(contentType)) {
                throw new IllegalArgumentException("addDataDelta only supported for data content");
            }

            dataDeltas.add(deltaData);

            // 创建增量内容对象返回
            DataContent deltaContent = new DataContent();
            deltaContent.setType(ContentType.DATA);
            deltaContent.setIndex(index);
            deltaContent.setDelta(true); // 标记为增量内容
            deltaContent.setMsgId(messageBuilder.messageId);
            deltaContent.setData(deltaData);
            deltaContent.inProgress();

            return deltaContent;
        }

        /**
         * 完成内容构建。
         *
         * <p>根据内容类型执行不同的完成操作：</p>
         * <ul>
         *   <li>文本：合并所有增量分片和已设置文本</li>
         *   <li>数据：合并所有增量数据到已有数据中</li>
         *   <li>其他：直接完成</li>
         * </ul>
         *
         * @return 关联的消息对象
         */
        public Message complete() {
            if (ContentType.TEXT.equals(contentType)) {
                // 对于文本内容，合并已有文本和增量分片
                if (textTokens != null && !textTokens.isEmpty()) {
                    String existingText = content instanceof TextContent ?
                        ((TextContent) content).getText() : "";
                    if (existingText == null) {
                        existingText = "";
                    }
                    String tokenText = String.join("", textTokens);
                    ((TextContent) content).setText(existingText + tokenText);
                }
                content.setDelta(false);
            } else if (ContentType.DATA.equals(contentType)) {
                // 对于数据内容，合并已有数据和增量数据
                if (dataDeltas != null && !dataDeltas.isEmpty()) {
                    Map<String, Object> existingData = content instanceof DataContent ?
                        ((DataContent) content).getData() : null;
                    if (existingData == null) {
                        existingData = new HashMap<>();
                    }

                    // 增量合并所有数据分片
                    Map<String, Object> finalData = mergeDataIncrementally(existingData, dataDeltas);
                    ((DataContent) content).setData(finalData);
                }
                content.setDelta(false);
            }

            // 设置完成状态
            content.completed();

            // 更新消息内容列表
            messageBuilder.addContent(content);

            return messageBuilder.message;
        }

        /**
         * 智能合并数据增量。
         *
         * <p>合并策略基于数据类型：</p>
         * <ul>
         *   <li>字符串：追加拼接</li>
         *   <li>数字：数值累加（保留浮点/整数精度）</li>
         *   <li>列表：追加合并</li>
         *   <li>字典：递归深度合并</li>
         *   <li>其他：直接替换</li>
         * </ul>
         *
         * @param baseData 基础数据
         * @param deltaList 增量数据列表
         * @return 合并后的数据映射
         */
        private Map<String, Object> mergeDataIncrementally(
            Map<String, Object> baseData,
            List<Map<String, Object>> deltaList
        ) {
            Map<String, Object> result = baseData != null ?
                new HashMap<>(baseData) : new HashMap<>();

            for (Map<String, Object> deltaData : deltaList) {
                for (Map.Entry<String, Object> entry : deltaData.entrySet()) {
                    String key = entry.getKey();
                    Object deltaValue = entry.getValue();

                    if (!result.containsKey(key)) {
                        // 新键，直接添加
                        result.put(key, deltaValue);
                    } else {
                        Object baseValue = result.get(key);
                        // 根据数据类型执行增量合并
                        if (baseValue instanceof String && deltaValue instanceof String) {
                            // 字符串拼接
                            result.put(key, (String) baseValue + (String) deltaValue);
                        } else if (baseValue instanceof Number && deltaValue instanceof Number &&
                                   !(baseValue instanceof Boolean) && !(deltaValue instanceof Boolean)) {
                            // 数值累加
                            if (baseValue instanceof Double || deltaValue instanceof Double) {
                                result.put(key, ((Number) baseValue).doubleValue() +
                                    ((Number) deltaValue).doubleValue());
                            } else {
                                result.put(key, ((Number) baseValue).longValue() +
                                    ((Number) deltaValue).longValue());
                            }
                        } else if (baseValue instanceof List && deltaValue instanceof List) {
                            // 列表追加合并
                            @SuppressWarnings("unchecked")
                            List<Object> merged = new ArrayList<>((List<Object>) baseValue);
                            merged.addAll((List<Object>) deltaValue);
                            result.put(key, merged);
                        } else if (baseValue instanceof Map && deltaValue instanceof Map) {
                            // 字典递归合并
                            @SuppressWarnings("unchecked")
                            Map<String, Object> merged = mergeDataIncrementally(
                                (Map<String, Object>) baseValue,
                                List.of((Map<String, Object>) deltaValue)
                            );
                            result.put(key, merged);
                        } else {
                            // 其他类型直接替换
                            result.put(key, deltaValue);
                        }
                    }
                }
            }

            return result;
        }

        /**
         * 获取当前内容对象的引用。
         *
         * @return 底层 Content 实例
         */
        public Content getContentData() {
            return content;
        }

        /**
         * 添加文本增量（向后兼容方法，委托给 {@link #addTextDelta}）。
         *
         * @param text 文本分片
         * @return 增量文本内容对象
         */
        public TextContent addDelta(String text) {
            return addTextDelta(text);
        }
    }
}
