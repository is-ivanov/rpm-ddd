package by.iivanov.rpm.iam.user.infrastructure.web;

import by.iivanov.rpm.iam.user.application.UserRegistrationService;
import by.iivanov.rpm.iam.user.domain.UserId;
import by.iivanov.rpm.iam.user.infrastructure.security.SecurityCurrentActorProvider;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/admin/users")
class UserResource {

    private final UserRegistrationService userRegistrationService;
    private final SecurityCurrentActorProvider currentActorProvider;

    UserResource(UserRegistrationService userRegistrationService, SecurityCurrentActorProvider currentActorProvider) {
        this.userRegistrationService = userRegistrationService;
        this.currentActorProvider = currentActorProvider;
    }

    /**
     * Register new user.
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> registerUser(@RequestBody @Valid RegisterUserRequest payload) {
        UserId createdBy = currentActorProvider.currentUserId();
        UserId userId = userRegistrationService.registerUser(payload.toCommand(), createdBy);
        URI location = UriComponentsBuilder.fromPath("/api/admin/users/{id}").build(userId.id());
        return ResponseEntity.created(location).build();
    }

    /**
     * List all users for the admin grid (resolved actor names, deterministic order).
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    List<UserRowResponse> listUsers() {
        throw new UnsupportedOperationException();
    }
}
