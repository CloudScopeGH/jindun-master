package com.cloudspace.jindun.ext.ninepatch;

public class WrongPaddingException extends RuntimeException {
    public WrongPaddingException() {
    }

    public WrongPaddingException(String detailMessage) {
        super(detailMessage);
    }

    public WrongPaddingException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public WrongPaddingException(Throwable throwable) {
        super(throwable);
    }
}
