package io.github.forgottenlab.aicontext.autoconfigure;

import io.github.forgottenlab.aicontext.core.ContextRegistry;
import io.github.forgottenlab.aicontext.core.ContextSource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(AiContextProperties.class)
@ConditionalOnProperty(
        prefix = "forgottenlab.ai.context",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class AiContextAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    ContextRegistry contextRegistry(ObjectProvider<ContextSource> contextSources) {
        return new ContextRegistry(contextSources.orderedStream().toList());
    }
}