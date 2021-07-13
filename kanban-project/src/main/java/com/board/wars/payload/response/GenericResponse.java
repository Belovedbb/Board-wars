package com.board.wars.payload.response;

import org.springframework.hateoas.server.core.Relation;

@Relation(itemRelation="_item", collectionRelation="_collection")
public class GenericResponse<T> implements Response{
    private boolean success;
    private T body;

    public GenericResponse( T body){
        this(true, body);
    }

    public GenericResponse(boolean success, T body){
        this.success = success;
        this.body = body;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }

}
