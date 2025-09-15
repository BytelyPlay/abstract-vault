package org.abstractvault.bytelyplay.exceptions;

public class UncheckedException extends RuntimeException {
    public UncheckedException(Throwable cause) {
        super(cause);
    }
    public UncheckedException(String message, Throwable cause) {
        super(message, cause);
    }
}
