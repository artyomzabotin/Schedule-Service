package epam.az.scheduler.error;

public enum ServiceErrorCode implements ErrorCode {

    SCHEDULE_NOT_FOUND;

    @Override
    public String getCode() {

        return name();
    }
}
