package com.board.wars.error;

public class KanbanResponseException extends RuntimeException{
    private int statusCode;

    private ErrorResponsePayload body;

    public KanbanResponseException(String message, int statusCode, ErrorResponsePayload body) {
        super(message);
        this.statusCode = statusCode;
        this.body = body;
    }

    int getStatusCode() {
        return statusCode;
    }

    ErrorResponsePayload getBody() {
        return body;
    }
}
