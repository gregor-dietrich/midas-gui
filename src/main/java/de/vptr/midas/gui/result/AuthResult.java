package de.vptr.midas.gui.result;

public class AuthResult {

    public enum Status {
        SUCCESS,
        INVALID_CREDENTIALS,
        BACKEND_UNAVAILABLE,
        INVALID_INPUT
    }

    private final Status status;
    private final String message;

    private AuthResult(final Status status, final String message) {
        this.status = status;
        this.message = message;
    }

    public static AuthResult success() {
        return new AuthResult(Status.SUCCESS, "Authentication successful");
    }

    public static AuthResult invalidCredentials() {
        return new AuthResult(Status.INVALID_CREDENTIALS, "Invalid username or password");
    }

    public static AuthResult backendUnavailable(final String details) {
        return new AuthResult(Status.BACKEND_UNAVAILABLE, "Backend service unavailable: " + details);
    }

    public static AuthResult invalidInput() {
        return new AuthResult(Status.INVALID_INPUT, "Username and password are required");
    }

    public Status getStatus() {
        return this.status;
    }

    public String getMessage() {
        return this.message;
    }

    public boolean isSuccess() {
        return this.status == Status.SUCCESS;
    }
}