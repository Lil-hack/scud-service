package ru.miit.scud_service;

public class ScudException extends Exception {
    public ScudException() {
        super();
    }
    private ErrorCode errorCodes;

    public int getErrorCode() {
        return errorCodes.getValue();
    }

    public ScudException(ErrorCode code) {
        super();
        errorCodes = code;
    }
}

