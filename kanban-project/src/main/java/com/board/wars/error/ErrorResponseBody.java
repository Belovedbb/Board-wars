package com.board.wars.error;

import java.util.List;

public class ErrorResponseBody {

    private final List<ErrorStatus> errors;

    public ErrorResponseBody(List<ErrorStatus> errors) {
        this.errors = errors;
    }

    public List<ErrorStatus> getErrors() {
        return errors;
    }

    public static class ErrorStatus{
        private final Long code;
        private final String description;
        private final String parameter;

        public ErrorStatus(Long code, String description, String parameter) {
            this.code = code;
            this.description = description;
            this.parameter = parameter;
        }

        public Long getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public String getParameter() {
            return parameter;
        }
    }
}
