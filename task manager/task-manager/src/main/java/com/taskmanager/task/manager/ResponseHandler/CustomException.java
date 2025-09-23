package com.taskmanager.task.manager.ResponseHandler;

public class CustomException extends RuntimeException {
    private final int status;
    private final String errorId;

    public CustomException(int status, String message, String errorId) {
        super(message);
        this.status = status;
        this.errorId = errorId;
    }

    public int getStatus() {
        return status;
    }

    public String getErrorId() {
        return errorId;
    }
}
