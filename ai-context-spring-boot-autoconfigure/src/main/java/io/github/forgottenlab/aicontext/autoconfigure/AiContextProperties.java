package io.github.forgottenlab.aicontext.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

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