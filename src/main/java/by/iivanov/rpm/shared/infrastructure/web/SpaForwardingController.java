package by.iivanov.rpm.shared.infrastructure.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Forwards client-side single-page-application routes to the bundled {@code index.html} so that
 * deep links and browser refreshes resolve to the SPA shell instead of a 404.
 *
 * <p>Only the explicit, publicly routable SPA paths are forwarded — this mirrors the
 * deny-by-default allow-list in {@code SecurityConfig}. API endpoints under {@code /api/**} and
 * static asset requests are not handled here.
 */
@Controller
class SpaForwardingController {

    @GetMapping(value = {"/", "/login", "/activate"})
    String forwardSpaRoute() {
        return "forward:/index.html";
    }
}
