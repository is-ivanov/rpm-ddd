package by.iivanov.rpm.iam.auth.infrastructure;

import by.iivanov.rpm.iam.user.domain.Login;
import by.iivanov.rpm.iam.user.domain.Password;
import by.iivanov.rpm.iam.user.domain.UserId;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public record RpmUserDetails(UserId userId, Login login, Password password) implements UserDetails {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return password.hash();
    }

    @Override
    public String getUsername() {
        return login.login();
    }
}
