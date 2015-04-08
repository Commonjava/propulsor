package org.commonjava.propulsor.boot;

public class BootStatus {

    public static final int ERR_LOAD_FROM_SYSPROPS = 1;

    public static final int ERR_PARSE_ARGS = 2;

    public static final int ERR_CANT_LISTEN = 3;

    public static final int ERR_STARTING = 4;

    public static final int ERR_LOAD_CONFIG = 5;

    private Throwable error;

    private int exit;

    public BootStatus() {
        exit = -1;
    }

    public BootStatus(final int exit, final Throwable error) {
        markFailed(exit, error);
    }

    public void markFailed(final int exit, final Throwable error) {
        this.exit = exit;
        this.error = error;
    }

    public boolean isSet() {
        return exit > -1;
    }

    public boolean isFailed() {
        return exit > 0;
    }

    public boolean isSuccess() {
        return exit == 0;
    }

    public Throwable getError() {
        return error;
    }

    public int getExitCode() {
        return exit;
    }

    public void markSuccess() {
        exit = 0;
    }

}
