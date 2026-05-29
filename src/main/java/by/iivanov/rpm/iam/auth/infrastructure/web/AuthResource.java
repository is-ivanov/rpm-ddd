package by.iivanov.rpm.iam.auth.infrastructure.web;

import by.iivanov.rpm.iam.auth.infrastructure.RpmUserDetails;
import by.iivanov.rpm.iam.user.application.ActivationService;
import by.iivanov.rpm.iam.user.application.AuthenticateUserCommand;
import by.iivanov.rpm.iam.user.application.AuthenticationService;
import by.iivanov.rpm.iam.user.domain.Login;
import by.iivanov.rpm.iam.user.domain.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.Collections;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/auth")
class AuthResource {

    private final AuthenticationService authenticationService;
    private final ActivationService activationService;
    private final SecurityContextHolderStrategy securityContextHolderStrategy;
    private final SecurityContextRepository securityContextRepository;

    AuthResource(
            AuthenticationService authenticationService,
            ActivationService activationService,
            SecurityContextHolderStrategy securityContextHolderStrategy,
            SecurityContextRepository securityContextRepository) {
        this.authenticationService = authenticationService;
        this.activationService = activationService;
        this.securityContextHolderStrategy = securityContextHolderStrategy;
        this.securityContextRepository = securityContextRepository;
    }

    @GetMapping("/csrf")
    CsrfToken csrf(CsrfToken token) {
        return token;
    }

    @GetMapping("/me")
    CurrentUserResponse me(@AuthenticationPrincipal RpmUserDetails userDetails) {
        var user = authenticationService.getCurrentUser(userDetails.userId());
        return CurrentUserResponse.from(user);
    }

    @GetMapping("/activate")
    ActivationTokenResponse validateActivationToken(@RequestParam String token) {
        var user = activationService.validateToken(token);
        return ActivationTokenResponse.from(user);
    }

    @PostMapping("/activate")
    void activate(@RequestBody @Valid ActivateAccountRequest request) {
        activationService.activate(request.token(), request.password());
    }

    @PostMapping("/logout")
    void logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        var authentication = securityContextHolderStrategy.getContext().getAuthentication();
        new SecurityContextLogoutHandler().logout(httpRequest, httpResponse, authentication);
    }

    @PostMapping("/login")
    void login(
            @RequestBody @Valid LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        var user = authenticationService.authenticate(
                new AuthenticateUserCommand(new Login(request.login()), request.password()));
        establishSecurityContext(user, httpRequest, httpResponse);
    }

    private void establishSecurityContext(User user, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        SecurityContext context = securityContextHolderStrategy.createEmptyContext();
        context.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
                new RpmUserDetails(user.getId(), user.getLogin(), user.getPassword()), null, Collections.emptyList()));
        securityContextHolderStrategy.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);
    }
}
