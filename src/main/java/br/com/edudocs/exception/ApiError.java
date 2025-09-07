package br.com.edudocs.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {
    private OffsetDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private Map<String, List<String>> details;

    public ApiError() {}

    public ApiError(int status, String error, String message, String path, Map<String, List<String>> details) {
        this.timestamp = OffsetDateTime.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.details = details;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }

    public Map<String, List<String>> getDetails() {
        return details;
    }

    public ApiError withPath(String path) {
        return new ApiError(this.status, this.error, this.message, path, this.details);
    }
}
