package com.xx.jaseatschoicejava.agent.listener;

/**
 * 执行事件
 *
 * 记录Agent执行过程中的各种事件信息
 *
 * @author Claude
 * @since 2026-03-26
 */
public class ExecutionEvent {
    /**
     * Agent名称
     */
    private String agentName;

    /**
     * Agent ID
     */
    private String agentId;

    /**
     * 工具名称（可选）
     */
    private String toolName;

    /**
     * 输入参数（JSON格式）
     */
    private String inputs;

    /**
     * 输出结果
     */
    private String output;

    /**
     * 错误信息（可选）
     */
    private String error;

    /**
     * 人类可读的消息
     */
    private String message;

    /**
     * 时间戳
     */
    private long timestamp = System.currentTimeMillis();

    /**
     * 是否为进度消息（true=进度消息，false=最终结果）
     * 进度消息不保存到数据库，只用于前端显示
     */
    private boolean progress = false;

    // Getters and Setters

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getToolName() {
        return toolName;
    }

    public void setToolName(String toolName) {
        this.toolName = toolName;
    }

    public String getInputs() {
        return inputs;
    }

    public void setInputs(String inputs) {
        this.inputs = inputs;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isProgress() {
        return progress;
    }

    public void setProgress(boolean progress) {
        this.progress = progress;
    }
}
