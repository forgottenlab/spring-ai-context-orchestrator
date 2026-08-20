package io.github.forgottenlab.aicontext.core;

/**
 * Declares how a context source participates in planning for one model turn.
 */
public enum ContextLoadStrategy {

    /**
     * Load whenever the source technically supports the request.
     */
    ALWAYS,

    /**
     * Load only when the source reports that the request is relevant.
     */
    RELEVANT,

    /**
     * Keep the source available for later demand-driven execution, such as tool calling.
     */
    ON_DEMAND,

    /**
     * Let the source make an explicit per-request planning decision.
     */
    CUSTOM
}