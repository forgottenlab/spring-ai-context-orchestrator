package io.github.forgottenlab.aicontext.springai;

/**
 * Namespaced advisor-context attributes used by the Spring AI bridge.
 *
 * <p>Only REQUEST_ATTRIBUTES is forwarded into core ContextRequest attributes.
 * Arbitrary Spring AI advisor context is deliberately not exposed to business
 * ContextSource implementations by default.</p>
 */
public final class SpringAiContextAttributes {

    private static final String PREFIX =
            "forgottenlab.ai.context.";

    public static final String CONVERSATION_ID =
            PREFIX + "conversation-id";

    public static final String REQUEST_ATTRIBUTES =
            PREFIX + "request-attributes";

    public static final String CONTEXT_PLAN =
            PREFIX + "plan";

    public static final String EXECUTION_REPORT =
            PREFIX + "execution-report";

    public static final String RESOLUTION =
            PREFIX + "resolution";

    public static final String BUDGET_RESULT =
            PREFIX + "budget-result";

    public static final String ASSEMBLY =
            PREFIX + "assembly";

    private SpringAiContextAttributes() {
    }
}