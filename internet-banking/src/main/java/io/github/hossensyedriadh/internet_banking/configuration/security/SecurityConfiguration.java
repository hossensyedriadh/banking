package io.github.hossensyedriadh.internet_banking.configuration.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    private final OidcUserAuthorizationManager oidcUserAuthorizationManager;

    @Value("${server.servlet.session.cookie.name}")
    private String sessionCookieName;

    @Autowired
    public SecurityConfiguration(OidcUserAuthorizationManager oidcUserAuthorizationManager) {
        this.oidcUserAuthorizationManager = oidcUserAuthorizationManager;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, LogoutSuccessHandler oidcLogoutSuccessHandler) {
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(authorize -> authorize.requestMatchers("/error", "/actuator/health/**",
                                "/actuator/info", "/v3/api-docs/**", "/swagger-ui/**", "/ui-docs.html").permitAll()
                        .anyRequest().access(this.oidcUserAuthorizationManager))
                .oauth2Login(Customizer.withDefaults())
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(Customizer.withDefaults())
                )
                .logout(logoutConfigurer -> logoutConfigurer.logoutUrl("/logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies(this.sessionCookieName)
                        .logoutSuccessHandler(oidcLogoutSuccessHandler));

        return http.build();
    }

    @Bean
    public LogoutSuccessHandler oidcLogoutSuccessHandler(ClientRegistrationRepository clientRegistrationRepository) {
        OidcClientInitiatedLogoutSuccessHandler handler = new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);
        handler.setPostLogoutRedirectUri("{baseUrl}/");

        return handler;
    }
}
