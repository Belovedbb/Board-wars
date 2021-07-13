package com.board.wars.error;

import com.board.wars.payload.response.Response;

public class ErrorResponsePayload implements Response {

    private ErrorResponseBody body;

    public ErrorResponsePayload(ErrorResponseBody body) {
        this.body = body;
    }

    @Override
    public boolean isSuccess() {
        return false;
    }

    @Override
    public ErrorResponseBody getBody() {
        return body;
    }
}
