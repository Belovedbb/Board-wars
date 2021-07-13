package com.board.wars.payload.response;

import java.time.LocalDateTime;

public interface Response  {

    boolean isSuccess();

    Object getBody() ;

    default LocalDateTime getServerTime(){
        return LocalDateTime.now();
    }
}
