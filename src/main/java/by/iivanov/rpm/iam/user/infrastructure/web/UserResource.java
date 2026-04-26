package by.iivanov.rpm.iam.user.infrastructure.web;

import by.iivanov.rpm.iam.user.application.UserRegistrationService;
import by.iivanov.rpm.iam.user.domain.UserId;
import by.iivanov.rpm.iam.user.infrastructure.security.SecurityCurrentActorProvider;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping(path = "/api/admin/users", consumes = MediaType.APPLICATION_JSON_VALUE)
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
    @PostMapping
    ResponseEntity<Void> registerUser(@RequestBody @Valid RegisterUserRequest payload) {
        UserId createdBy = currentActorProvider.currentUserId();
        UserId userId = userRegistrationService.registerUser(payload.toCommand(), createdBy);
        URI location = UriComponentsBuilder.fromPath("/api/admin/users/{id}").build(userId);
        return ResponseEntity.created(location).build();
    }
}
