package com.taskmanager.task.manager.ResponseHandler;

public class SuccessResponse {
    private int statusCode;
    private String message;
    private String id;


        public SuccessResponse(int statusCode, String message, String id) {
            this.id = id;
            this.statusCode = statusCode;
            this.message = message;

        }

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
