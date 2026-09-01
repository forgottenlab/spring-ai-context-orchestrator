package io.github.forgottenlab.aicontext.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Public Spring Boot configuration properties for AI Context Orchestrator.
 * AI Context Orchestrator 对外公开的 Spring Boot 配置属性。
 *
 * <p>
 * Property names are part of the consumer-facing compatibility surface and should change cautiously.
 * 配置项名称属于面向使用者的兼容性接口，修改时应保持谨慎。
 * </p>
 */
@ConfigurationProperties("forgottenlab.ai.context")
public class AiContextProperties {

    private boolean enabled = true;

    private final Budget budget = new Budget();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Budget getBudget() {
        return budget;
    }

    public static class Budget {

        /**
         * Framework-level context budget. The concrete tokenizer strategy is intentionally
         * not fixed in Phase 0.
         */
        private int maxContextTokens = 12_000;

        public int getMaxContextTokens() {
            return maxContextTokens;
        }

        public void setMaxContextTokens(int maxContextTokens) {
            if (maxContextTokens <= 0) {
                throw new IllegalArgumentException("maxContextTokens must be greater than 0");
            }
            this.maxContextTokens = maxContextTokens;
        }
    }
}
