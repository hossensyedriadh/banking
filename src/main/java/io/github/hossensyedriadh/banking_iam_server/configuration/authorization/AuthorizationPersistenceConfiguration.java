package io.github.hossensyedriadh.banking_iam_server.configuration.authorization;

import io.github.hossensyedriadh.banking_iam_server.configuration.security.PersistentClientPrincipal;
import io.github.hossensyedriadh.banking_iam_server.configuration.security.PrincipalEnrichingOAuth2AuthorizationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.jackson.SecurityJacksonModules;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

import javax.sql.DataSource;

@Configuration
public class AuthorizationPersistenceConfiguration {
    @Bean
    public JdbcOperations jdbcOperations(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository(JdbcOperations jdbcOperations) {
        return new JdbcRegisteredClientRepository(jdbcOperations);
    }

    @Bean
    public OAuth2AuthorizationService authorizationService(JdbcOperations jdbcOperations, RegisteredClientRepository registeredClientRepository) {
        JdbcOAuth2AuthorizationService jdbcService = new JdbcOAuth2AuthorizationService(jdbcOperations, registeredClientRepository);

        BasicPolymorphicTypeValidator.Builder validator = BasicPolymorphicTypeValidator.builder().allowIfSubType(PersistentClientPrincipal.class);

        JsonMapper jsonMapper = JsonMapper.builder().addModules(
                        SecurityJacksonModules.getModules(getClass().getClassLoader(), validator))
                .build();

        jdbcService.setAuthorizationParametersMapper(new JdbcOAuth2AuthorizationService.JsonMapperOAuth2AuthorizationParametersMapper(jsonMapper));

        jdbcService.setAuthorizationRowMapper(
                new JdbcOAuth2AuthorizationService.JsonMapperOAuth2AuthorizationRowMapper(registeredClientRepository, jsonMapper)
        );

        return new PrincipalEnrichingOAuth2AuthorizationService(jdbcService);
    }

    @Bean
    public OAuth2AuthorizationConsentService authorizationConsentService(JdbcOperations jdbcOperations, RegisteredClientRepository registeredClientRepository) {
        return new JdbcOAuth2AuthorizationConsentService(jdbcOperations, registeredClientRepository);
    }
}
