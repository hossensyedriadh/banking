package io.github.hossensyedriadh.banking_iam_server.configuration.authentication;

import io.github.hossensyedriadh.banking_iam_server.entity.IamAuthority;
import io.github.hossensyedriadh.banking_iam_server.entity.IamUser;
import io.github.hossensyedriadh.banking_iam_server.repository.IamUserRepository;
import lombok.extern.log4j.Log4j2;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class IamUserDetailsService implements UserDetailsService {
    private final IamUserRepository iamUserRepository;

    @Autowired
    public IamUserDetailsService(IamUserRepository iamUserRepository) {
        this.iamUserRepository = iamUserRepository;
    }

    @Override
    @NonNull
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        IamUser user = this.iamUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return User.builder().username(user.getUsername())
                .password(user.getPasswordHash()).disabled(!user.getEnabled())
                .authorities(user.getAuthorities().stream()
                        .map(IamAuthority::getName).toArray(String[]::new))
                .build();
    }
}
