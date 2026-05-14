package by.iivanov.rpm.iam.auth.infrastructure;

import by.iivanov.rpm.iam.user.domain.Login;
import by.iivanov.rpm.iam.user.domain.User;
import by.iivanov.rpm.iam.user.domain.UserRepository;
import by.iivanov.rpm.shared.infrastructure.InfrastructureComponent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@InfrastructureComponent
class RpmUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    RpmUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String loginValue) throws UsernameNotFoundException {
        User user = userRepository
                .findByLogin(new Login(loginValue))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + loginValue));
        return new RpmUserDetails(user.getId(), user.getLogin(), user.getPassword());
    }
}
