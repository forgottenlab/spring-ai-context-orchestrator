package io.github.forgottenlab.aicontext.examples.quickstart;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = QuickstartApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
class QuickstartApplicationTest {

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private ChatClient.Builder chatClientBuilder;

    @Autowired
    private QuickstartDemoRunner runner;

    @Autowired
    private OfflineChatModel chatModel;

    @Autowired
    private InventoryContextSource contextSource;

    @Test
    void runsThroughStarterAndCapturesEnrichedPrompt() {
        assertThat(applicationContext.isActive()).isTrue();
        assertThat(chatClientBuilder).isNotNull();
        assertThat(runner.lastResponse())
                .isEqualTo(OfflineChatModel.RESPONSE);
        assertThat(chatModel.callCount()).isEqualTo(1);
        assertThat(contextSource.loadCount()).isEqualTo(1);

        Prompt prompt = chatModel.lastPrompt();
        assertThat(prompt).isNotNull();
        assertThat(prompt.getUserMessage().getText())
                .isEqualTo(QuickstartDemoRunner.USER_MESSAGE);

        String systemText = prompt.getSystemMessage().getText();
        assertThat(systemText)
                .contains(QuickstartDemoRunner.SYSTEM_MESSAGE)
                .contains("<business-context>")
                .contains("</business-context>")
                .contains("source=\"inventory\"")
                .contains("stock=7")
                .containsOnlyOnce("<business-context>");
        assertThat(systemText.indexOf(
                QuickstartDemoRunner.SYSTEM_MESSAGE
        )).isLessThan(systemText.indexOf(
                "<business-context>"
        ));
    }
}
