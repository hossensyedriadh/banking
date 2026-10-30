package io.github.hossensyedriadh.atm_pos_switch.interceptor;

import io.github.hossensyedriadh.atm_pos_switch.iam.api.service.IamTokenService;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class IamAuthenticationInterceptor implements ClientHttpRequestInterceptor {
    private final IamTokenService iamTokenService;

    @Autowired
    public IamAuthenticationInterceptor(IamTokenService iamTokenService) {
        this.iamTokenService = iamTokenService;
    }

    @Override
    @NonNull
    public ClientHttpResponse intercept(@NonNull HttpRequest request, byte @NonNull [] body, @NonNull ClientHttpRequestExecution execution) throws IOException {
        String accessToken = this.iamTokenService.getAccessToken();
        request.getHeaders().setBearerAuth(accessToken);

        return execution.execute(request, body);
    }
}
