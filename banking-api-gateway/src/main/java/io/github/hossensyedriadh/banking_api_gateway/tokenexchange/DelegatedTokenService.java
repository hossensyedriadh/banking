package io.github.hossensyedriadh.banking_api_gateway.tokenexchange;

import io.github.hossensyedriadh.banking_api_gateway.enumerator.ChannelType;
import io.github.hossensyedriadh.banking_api_gateway.security.token.ActorTokenService;
import io.github.hossensyedriadh.banking_api_gateway.security.token.TokenExchangeClient;
import io.github.hossensyedriadh.banking_api_gateway.security.token.TokenExchangeScopeMappings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Set;

@Service
public class DelegatedTokenService {
    private final ActorTokenService actorTokenService;
    private final TokenExchangeClient tokenExchangeClient;

    @Autowired
    public DelegatedTokenService(ActorTokenService actorTokenService, TokenExchangeClient tokenExchangeClient) {
        this.actorTokenService = actorTokenService;
        this.tokenExchangeClient = tokenExchangeClient;
    }

    public Mono<DelegatedToken> exchange(String subjectToken, ChannelType channel, String requiredChannelAuthority) {
        if (subjectToken == null || subjectToken.isBlank()) {
            return Mono.error(new IllegalArgumentException("Subject token cannot be blank"));
        }

        String requiredCbsScope = TokenExchangeScopeMappings.forChannel(channel).get(requiredChannelAuthority);

        if (requiredCbsScope == null) {
            return Mono.error(new IllegalArgumentException("No CBS scope mapping found for channel " + channel
                    + " and authority " + requiredChannelAuthority));
        }

        return this.actorTokenService.getAccessToken().flatMap(actorToken -> this.tokenExchangeClient.exchange(subjectToken, actorToken,
                Set.of(requiredCbsScope)))
                .handle((response, sink) -> {
                    if (response.accessToken() == null || response.accessToken().isBlank()) {
                        sink.error(new IllegalStateException("IAM Server returned an empty delegated access token"));
                        return;
                    }

                    if (response.expiresIn() <= 0) {
                        sink.error(new IllegalStateException("IAM Server returned an invalid delegated access token"));
                        return;
                    }

                    Set<String> scopes = response.scope() == null || response.scope().isBlank() ? Set.of() : Set.of(response.scope().split("\\s+"));

                    sink.next(new DelegatedToken(response.accessToken(), Instant.now().plusSeconds(response.expiresIn()), scopes));
                });
    }
}
