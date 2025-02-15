package it.gov.pagopa.pu.fileshare.connector.auth.config;

import it.gov.pagopa.pu.fileshare.config.RestTemplateConfig;
import it.gov.pagopa.pu.p4paauth.controller.ApiClient;
import it.gov.pagopa.pu.p4paauth.controller.BaseApi;
import it.gov.pagopa.pu.p4paauth.controller.generated.AuthnApi;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AuthApisHolder {

    private final AuthnApi authnApi;

    private final ThreadLocal<String> bearerTokenHolder = new ThreadLocal<>();

    public AuthApisHolder(
        AuthClientConfig clientConfig,
        RestTemplateBuilder restTemplateBuilder
    ) {
        RestTemplate restTemplate = restTemplateBuilder.build();
        ApiClient apiClient = new ApiClient(restTemplate);
      apiClient.setBasePath(clientConfig.getBaseUrl());
      apiClient.setBearerToken(bearerTokenHolder::get);
      apiClient.setMaxAttemptsForRetry(Math.max(1, clientConfig.getMaxAttempts()));
      apiClient.setWaitTimeMillis(clientConfig.getWaitTimeMillis());
      if (clientConfig.isPrintBodyWhenError()) {
        restTemplate.setErrorHandler(RestTemplateConfig.bodyPrinterWhenError("ORGANIZATION"));
      }

        this.authnApi = new AuthnApi(apiClient);
    }

    @PreDestroy
    public void unload(){
        bearerTokenHolder.remove();
    }

    /** It will return a {@link AuthnApi} instrumented with the provided accessToken. Use null if auth is not required */
    public AuthnApi getAuthnApi(String accessToken){
        return getApi(accessToken, authnApi);
    }

    private <T extends BaseApi> T getApi(String accessToken, T api) {
        bearerTokenHolder.set(accessToken);
        return api;
    }
}
