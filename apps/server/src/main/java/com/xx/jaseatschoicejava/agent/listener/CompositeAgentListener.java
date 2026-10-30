package com.xx.jaseatschoicejava.agent.listener;

import dev.langchain4j.agentic.observability.AfterAgentToolExecution;
import dev.langchain4j.agentic.observability.AgentInvocationError;
import dev.langchain4j.agentic.observability.AgentListener;
import dev.langchain4j.agentic.observability.AgentRequest;
import dev.langchain4j.agentic.observability.AgentResponse;
import dev.langchain4j.agentic.observability.BeforeAgentToolExecution;
import dev.langchain4j.agentic.scope.AgenticScope;

/**
 * 组合上下文初始化和原有监听器。
 */
public class CompositeAgentListener implements AgentListener {

    private final AgentListener contextListener;
    private final AgentListener delegate;

    public CompositeAgentListener(AgentListener contextListener, AgentListener delegate) {
        this.contextListener = contextListener;
        this.delegate = delegate;
    }

    @Override
    public void afterAgenticScopeCreated(AgenticScope scope) {
        contextListener.afterAgenticScopeCreated(scope);
        delegate.afterAgenticScopeCreated(scope);
    }

    @Override
    public void beforeAgentInvocation(AgentRequest request) {
        delegate.beforeAgentInvocation(request);
    }

    @Override
    public void afterAgentInvocation(AgentResponse response) {
        delegate.afterAgentInvocation(response);
    }

    @Override
    public void onAgentInvocationError(AgentInvocationError error) {
        delegate.onAgentInvocationError(error);
    }

    @Override
    public void beforeAgenticScopeDestroyed(AgenticScope scope) {
        delegate.beforeAgenticScopeDestroyed(scope);
    }

    @Override
    public void beforeAgentToolExecution(BeforeAgentToolExecution execution) {
        delegate.beforeAgentToolExecution(execution);
    }

    @Override
    public void afterAgentToolExecution(AfterAgentToolExecution execution) {
        delegate.afterAgentToolExecution(execution);
    }

    @Override
    public boolean inheritedBySubagents() {
        return delegate.inheritedBySubagents();
    }
}
