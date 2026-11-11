package io.github.hossensyedriadh.banking_iam_server.configuration.security;

import io.github.hossensyedriadh.banking_iam_server.authentication.tokenexchange.TokenExchangePolicyAuthenticationProvider;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import java.util.HashMap;
import java.util.Map;

@Log4j2
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    private final TokenExchangePolicyAuthenticationProvider tokenExchangePolicyAuthenticationProvider;
    private final OAuth2TokenGenerator<?> oAuth2TokenGenerator;

    @Value("${server.servlet.session.cookie.name}")
    private String sessionCookieName;

    @Autowired
    public SecurityConfiguration(TokenExchangePolicyAuthenticationProvider tokenExchangePolicyAuthenticationProvider,
                                 OAuth2TokenGenerator<?> oAuth2TokenGenerator) {
        this.tokenExchangePolicyAuthenticationProvider = tokenExchangePolicyAuthenticationProvider;
        this.oAuth2TokenGenerator = oAuth2TokenGenerator;
    }

    @Bean
    @Order(1)
    public SecurityFilterChain authServerSecurityFilterChain(HttpSecurity http) {
        http.oauth2AuthorizationServer(authServer -> {
            http.securityMatcher(authServer.getEndpointsMatcher());
            authServer.oidc(Customizer.withDefaults())
                    .tokenGenerator(this.oAuth2TokenGenerator)
                    .tokenEndpoint(endpoint -> endpoint.authenticationProviders(providers -> {
                        providers.addFirst(this.tokenExchangePolicyAuthenticationProvider);
                    }));
        });

        http.authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(
                        new LoginUrlAuthenticationEntryPoint("/login")
                ));

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain applicationSecurityFilterChain(HttpSecurity http, @Qualifier("iamAuthenticationManager") AuthenticationManager authenticationManager) {
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authenticationManager(authenticationManager)
                .authorizeHttpRequests(auth -> auth.requestMatchers("/error", "/actuator/info", "/actuator/health/**")
                        .permitAll().anyRequest().authenticated())
                .formLogin(Customizer.withDefaults()).logout(logoutConfigurer -> logoutConfigurer.logoutUrl("/logout")
                        .invalidateHttpSession(true).clearAuthentication(true)
                        .deleteCookies(this.sessionCookieName));

        return http.build();
    }

    @Bean("userPwdEncoder")
    public PasswordEncoder userPasswordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean("clientPwdEncoder")
    public PasswordEncoder clientPasswordEncoder() {
        Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put("bcrypt", new BCryptPasswordEncoder(11));

        return new DelegatingPasswordEncoder("bcrypt", encoders);
    }
}
