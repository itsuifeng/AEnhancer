package com.baidu.aenhancer.exception;

public class CodingError extends EnhancerRuntimeException {

    private static final long serialVersionUID = 1L;

    public CodingError(String message) {
        super(message);
    }

    public CodingError(String message, Throwable e) {
        super(message, e);
    }
}
