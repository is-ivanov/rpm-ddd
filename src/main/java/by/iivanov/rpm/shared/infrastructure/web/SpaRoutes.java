package by.iivanov.rpm.shared.infrastructure.web;

/**
 * Single source of truth for the SPA client-route path pattern, shared by the MVC forward mapping
 * ({@code SpaForwardingController}) and the security allow-list entry ({@code SecurityConfig}) —
 * the two must always match.
 */
public final class SpaRoutes {

    /**
     * One extension-less URI segment (no dot), e.g. {@code /dashboard} — treated as an SPA client
     * route. Paths with a dot ({@code /index.html}, {@code /assets/app.js}) are files, not routes.
     */
    public static final String SPA_ROUTE_PATTERN = "/{path:[^.]*}";

    private SpaRoutes() {}
}
