package by.iivanov.rpm.iam.auth.fixtures;

import by.iivanov.rpm.iam.user.domain.JtiGenerator;
import by.iivanov.rpm.iam.user.domain.JwtActivationTokenGenerator;
import by.iivanov.rpm.iam.user.domain.UserId;
import org.springframework.stereotype.Component;

@Component
public class ActivationTokenFixture {

    private final JwtActivationTokenGenerator tokenGenerator;

    public ActivationTokenFixture(JwtActivationTokenGenerator tokenGenerator) {
        this.tokenGenerator = tokenGenerator;
    }

    public String generateValidToken(UserId userId) {
        return tokenGenerator.generateToken(userId, JtiGenerator.generate());
    }
}
