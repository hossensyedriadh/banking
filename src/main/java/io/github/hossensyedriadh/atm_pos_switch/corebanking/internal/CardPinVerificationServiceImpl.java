package io.github.hossensyedriadh.atm_pos_switch.corebanking.internal;

import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.enumerator.CardPinVerificationResult;
import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.model.CoreBankingCardPinVerificationRequest;
import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.service.CardPinVerificationService;
import io.github.hossensyedriadh.atm_pos_switch.exception.CoreBankingCommunicationException;
import io.github.hossensyedriadh.atm_pos_switch.exception.CoreBankingException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Log4j2
@Service
class CardPinVerificationServiceImpl implements CardPinVerificationService {
    private final RestClient restClient;

    @Value("${core-banking.atm-gateway.base-url}")
    private String coreBankingAtmGatewayBaseUrl;

    @Autowired
    public CardPinVerificationServiceImpl(@Qualifier("cbsRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public CardPinVerificationResult verify(CoreBankingCardPinVerificationRequest request) {
        try {
            CardPinVerificationResult result = this.restClient.post().uri(this.coreBankingAtmGatewayBaseUrl
                            + "/card-pins/pin-verification")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request).retrieve()
                    .body(CardPinVerificationResult.class);

            if (result == null) {
                log.error("CBS/Gateway returned an empty response for card PIN verification");
                throw new CoreBankingException("CBS/Gateway returned an empty response for card PIN verification");
            }

            return result;
        } catch (RestClientResponseException e) {
            log.error("CBS/Gateway returned HTTP {} for card PIN verification", e.getStatusCode().value(), e);
            throw new CoreBankingException("CBS/Gateway returned an error response", e);
        } catch (RestClientException e) {
            log.error("Failed to communicate with CBS/Gateway", e);
            throw new CoreBankingCommunicationException("Could not complete CBS/Gateway card PIN verification", e);
        }
    }
}
