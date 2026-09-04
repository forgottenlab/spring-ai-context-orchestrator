package io.github.forgottenlab.aicontext.core;

import java.util.Map;
import java.util.Objects;

/**
 * Input to one context-orchestration turn.
 * 一次上下文编排的输入。
 *
 * <p>
 * It carries the user input, an optional conversation identifier, and explicit
 * business request attributes that planners and sources may inspect.
 * 它承载用户输入、可选的会话标识，以及 Planner 和 Source 可读取的显式业务请求属性。
 * </p>
 *
 * <p>
 * This record only describes the request. It does not load business data or
 * invoke any {@link ContextSource}.
 * 此记录只描述请求本身，不加载业务数据，也不调用任何 {@link ContextSource}。
 * </p>
 */
public record ContextRequest(
        String userInput,
        String conversationId,
        Map<String, Object> attributes
) {

    public ContextRequest {
        Objects.requireNonNull(userInput, "userInput must not be null");
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
