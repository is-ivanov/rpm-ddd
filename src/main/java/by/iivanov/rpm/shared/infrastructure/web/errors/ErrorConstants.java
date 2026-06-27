package by.iivanov.rpm.shared.infrastructure.web.errors;

import java.net.URI;

public final class ErrorConstants {

    public static final String PROBLEM_BASE_URL = "https://www.rpm-ddd.my/problem";
    public static final URI ACCESS_DENIED_TYPE = URI.create(PROBLEM_BASE_URL + "/access-denied");

    private ErrorConstants() {}
}
