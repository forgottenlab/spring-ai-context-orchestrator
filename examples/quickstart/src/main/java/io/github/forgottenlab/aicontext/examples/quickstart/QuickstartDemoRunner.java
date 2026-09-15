package io.github.forgottenlab.aicontext.examples.quickstart;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
final class QuickstartDemoRunner implements ApplicationRunner {

    static final String SYSTEM_MESSAGE =
            "You are the inventory assistant.";

    static final String USER_MESSAGE =
            "How many units remain?";

    private final ChatClient chatClient;
    private final OfflineChatModel chatModel;
    private final InventoryContextSource contextSource;

    private volatile String lastResponse;

    QuickstartDemoRunner(
            ChatClient.Builder builder,
            OfflineChatModel chatModel,
            InventoryContextSource contextSource
    ) {
        this.chatClient = builder.build();
        this.chatModel = chatModel;
        this.contextSource = contextSource;
    }

    @Override
    public void run(ApplicationArguments args) {
        lastResponse = chatClient.prompt()
                .system(SYSTEM_MESSAGE)
                .user(USER_MESSAGE)
                .call()
                .content();

        Prompt prompt = chatModel.lastPrompt();

        System.out.println(
                "=== AI Context Orchestrator Quickstart ==="
        );
        System.out.println();
        System.out.println("Assistant response:");
        System.out.println(lastResponse);
        System.out.println();
        System.out.println("Final system message:");
        System.out.println(prompt.getSystemMessage().getText());
        System.out.println();
        System.out.println("Context source loads:");
        System.out.println(contextSource.loadCount());
    }

    String lastResponse() {
        return lastResponse;
    }
}
