package com.reliaquest.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(T data, Status status, String error) {

    public static <T> ApiResponse<T> handled() {
        return new ApiResponse<>(null, Status.HANDLED, null);
    }

    public static <T> ApiResponse<T> handledWith(T data) {
        return new ApiResponse<>(data, Status.HANDLED, null);
    }

    public static <T> ApiResponse<T> error(String error) {
        return new ApiResponse<>(null, Status.ERROR, error);
    }

    public enum Status {
        HANDLED("Successfully processed request."),
        ERROR("Failed to process request.");

        @JsonValue
        @Getter
        private final String value;

        Status(String value) {
            this.value = value;
        }
    }
}
