package io.github.hossensyedriadh.banking_api_gateway.security.token;

import io.github.hossensyedriadh.banking_api_gateway.configuration.properties.IamProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
public class ActorTokenService {
    private static final long REFRESH_SKEW_SECONDS = 30;

    private final WebClient iamWebClient;
    private final IamProperties iamProperties;

    private volatile CachedActorToken cachedToken;
    private Mono<CachedActorToken> refreshInFlight;

    @Autowired
    public ActorTokenService(@Qualifier("iamWebClient") WebClient webClient, IamProperties iamProperties) {
        this.iamWebClient = webClient;
        this.iamProperties = iamProperties;
    }

    public synchronized Mono<String> getAccessToken() {
        if (cachedToken != null && cachedToken.expiresAt().isAfter(Instant.now().plusSeconds(REFRESH_SKEW_SECONDS))) {
            return Mono.just(cachedToken.value());
        }

        if (refreshInFlight == null) {
            refreshInFlight = requestActorToken().doOnNext(token -> cachedToken = token)
                    .doFinally(_ -> {
                        synchronized (ActorTokenService.this) {
                            refreshInFlight = null;
                        }
                    })
                    .cache();
        }

        return refreshInFlight.map(CachedActorToken::value);
    }

    private Mono<CachedActorToken> requestActorToken() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");

        return this.iamWebClient.post().uri(this.iamProperties.tokenUri())
                .headers(headers -> headers.setBasicAuth(iamProperties.clientId(), iamProperties.clientSecret()))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(form))
                .retrieve().bodyToMono(OAuth2TokenResponse.class)
                .handle((response, sink) -> {
                    if (response.accessToken() == null || response.accessToken().isBlank()) {
                        sink.error(new IllegalStateException("IAM Server returned an empty actor access token"));
                        return;
                    }

                    if (response.expiresIn() <= REFRESH_SKEW_SECONDS) {
                        sink.error(new IllegalStateException("IAM Server returned an actor token with an invalid lifetime"));
                        return;
                    }

                    sink.next(new CachedActorToken(response.accessToken(), Instant.now().plusSeconds(response.expiresIn())));
                });
    }


    private record CachedActorToken(String value, Instant expiresAt) {
    }
}
