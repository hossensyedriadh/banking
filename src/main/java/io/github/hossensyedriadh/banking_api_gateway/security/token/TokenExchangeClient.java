package io.github.hossensyedriadh.banking_api_gateway.security.token;

import io.github.hossensyedriadh.banking_api_gateway.configuration.properties.IamProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.TreeSet;

@Component
public class TokenExchangeClient {
    private static final String TOKEN_EXCHANGE_GRANT = "urn:ietf:params:oauth:grant-type:token-exchange";
    private static final String ACCESS_TOKEN_TYPE = "urn:ietf:params:oauth:token-type:access_token";

    private final WebClient iamWebClient;
    private final IamProperties iamProperties;

    @Autowired
    public TokenExchangeClient(@Qualifier("iamWebClient") WebClient webClient, IamProperties iamProperties) {
        this.iamWebClient = webClient;
        this.iamProperties = iamProperties;
    }

    public Mono<OAuth2TokenResponse> exchange(String subjectToken, String actorToken, Set<String> requestedScopes) {
        if (requestedScopes == null || requestedScopes.isEmpty()) {
            return Mono.error(new IllegalArgumentException("At least one CBS scope must be requested"));
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", TOKEN_EXCHANGE_GRANT);
        form.add("subject_token", subjectToken);
        form.add("subject_token_type", ACCESS_TOKEN_TYPE);
        form.add("actor_token", actorToken);
        form.add("actor_token_type", ACCESS_TOKEN_TYPE);
        form.add("requested_token_type", ACCESS_TOKEN_TYPE);
        form.add("audience", "cbs");
        form.add("scope", String.join(" ", new TreeSet<>(requestedScopes)));

        return this.iamWebClient.post().uri(iamProperties.tokenUri())
                .headers(headers -> headers.setBasicAuth(iamProperties.clientId(), iamProperties.clientSecret()))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(form))
                .retrieve().bodyToMono(OAuth2TokenResponse.class);
    }
}
