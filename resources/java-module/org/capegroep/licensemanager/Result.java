package org.capegroep.licensemanager;

public class Result {
    private final boolean result;
    private final String message;
    public Result(boolean result, String message) {
        this.result = result;
        this.message = message;
    }

    public boolean getResult() {
        return result;
    }
    public String getMessage() {
        return message;
    }
}
