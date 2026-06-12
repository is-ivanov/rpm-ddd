package by.iivanov.rpm.shared.infrastructure.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Forwards client-side single-page-application routes to the bundled {@code index.html} so that
 * deep links and browser refreshes resolve to the SPA shell instead of a 404.
 *
 * <p>Any extension-less GET path is treated as an SPA route and forwarded — this mirrors the
 * allowlist in {@code SecurityConfig}, where {@code /api/**} is matched (and gated) before the
 * SPA catch-all entry. API endpoints under {@code /api/**} and static asset requests (paths with
 * a file extension, e.g. {@code /assets/app.js}) are not handled here.
 */
@Controller
class SpaForwardingController {

    // the 'path' variable exists only to constrain the mapping to extension-less segments; its value is unused
    @SuppressWarnings("MVCPathVariableInspection")
    @GetMapping(value = {"/", SpaRoutes.SPA_ROUTE_PATTERN})
    String forwardSpaRoute() {
        return "forward:/index.html";
    }
}
