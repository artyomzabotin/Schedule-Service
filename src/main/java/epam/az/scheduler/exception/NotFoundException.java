package epam.az.scheduler.exception;

import epam.az.scheduler.error.ErrorCode;

public class NotFoundException extends BaseException {

    public NotFoundException(ErrorCode errorCode, Object...values) {

        super(errorCode, values);
    }
}
