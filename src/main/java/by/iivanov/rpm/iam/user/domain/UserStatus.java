package by.iivanov.rpm.iam.user.domain;

public enum UserStatus {
    PENDING,
    ACTIVE,
    LOCKED,
    INACTIVE;

    /**
     * Provides an error message corresponding to the user's current status during authentication.
     */
    public String authenticationErrorMessage() {
        return switch (this) {
            case PENDING -> "Account not activated";
            case LOCKED -> "Account locked";
            case INACTIVE -> "Account deactivated";
            case ACTIVE -> throw new IllegalStateException("Unexpected ACTIVE status");
        };
    }
}
