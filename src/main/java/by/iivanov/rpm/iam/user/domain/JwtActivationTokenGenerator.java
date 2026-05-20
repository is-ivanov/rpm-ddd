package by.iivanov.rpm.iam.user.domain;

import by.iivanov.rpm.shared.infrastructure.DomainService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;

@DomainService
public class JwtActivationTokenGenerator {

    private final SecretKey signingKey;
    private final Duration expiration;
    private final Clock clock;

    public JwtActivationTokenGenerator(
            @Value("${rpm.iam.activation.jwt.secret}") String secret,
            @Value("${rpm.iam.activation.jwt.expiration}") Duration expiration,
            Clock clock) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
        this.clock = clock;
    }

    public UserId parseActivationClaim(String token) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public String generateToken(UserId userId, String jti) {
        var now = Instant.now(clock);
        return Jwts.builder()
                .subject(userId.id().toString())
                .id(jti)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expiration)))
                .claim("typ", "activation")
                .signWith(signingKey)
                .compact();
    }
}
