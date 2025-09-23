package com.taskmanager.task.manager.ResponseHandler;


public class ErrorResponse {

    private int statusCode;

    private String message;
    private String errorId;

    public ErrorResponse(int statusCode, String message, String errorId) {
        this.statusCode = statusCode;
        this.message = message;
        this.errorId = errorId;
    }
    public ErrorResponse(){};

    // Getters and setters
    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrorId() {
        return errorId;
    }

    public void setErrorId(String errorId) {
        this.errorId = errorId;
    }
}
