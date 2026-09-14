package io.github.forgottenlab.aicontext.core.assembly;

import java.util.List;

/**
 * Immutable structured output of the Assembler immediately before model integration.
 * Assembler 在模型边界前产生的不可变结构化输出。
 *
 * <p>
 * It contains the ordered {@link ContextBlock} values created from candidates
 * that survived budgeting. An integration-layer renderer consumes this
 * assembly after the Core pipeline and performs model-specific text injection.
 * 它包含由通过预算选择的候选项生成的有序 {@link ContextBlock} 集合。Core 流水线
 * 完成后，集成层 Renderer 消费此 Assembly，并执行面向具体模型的文本注入。
 * </p>
 *
 * <p>
 * A ContextAssembly is not the final concatenated system-prompt string. Core
 * intentionally prescribes neither a final Prompt format nor a Spring AI
 * Message type; the Spring AI renderer and Advisor perform that translation
 * afterward.
 * ContextAssembly 不等同于最终拼接后的 system Prompt 字符串。Core 有意不规定最终
 * Prompt 格式或 Spring AI Message 类型；该转换由后续 Spring AI Renderer 与
 * Advisor 完成。
 * </p>
 */
public record ContextAssembly(List<ContextBlock> blocks) {

    public ContextAssembly {
        blocks = blocks == null ? List.of() : List.copyOf(blocks);
    }

    public boolean isEmpty() {
        return blocks.isEmpty();
    }
}
