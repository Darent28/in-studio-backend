package com.is.in_studio.exception;

public class CustomExceptions extends RuntimeException {

    private CustomExceptions() {
    }

    private CustomExceptions(String message) {
        super(message);
    }

    private CustomExceptions(String message, Throwable cause) {
        super(message, cause);
    }

    public static class ProcessServiceException extends CustomExceptions {
        public ProcessServiceException(String message) {
            super(message);
        }

        public ProcessServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class ServerErrorException extends CustomExceptions {
        public ServerErrorException(String message) {
            super(message);
        }
    }

    public static class NotFoundException extends CustomExceptions {
        public NotFoundException(String message) {
            super(message);
        }
    }
}
