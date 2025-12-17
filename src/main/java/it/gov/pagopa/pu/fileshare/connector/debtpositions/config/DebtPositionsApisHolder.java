package it.gov.pagopa.pu.fileshare.connector.debtpositions.config;

import it.gov.pagopa.pu.debtpositions.controller.ApiClient;
import it.gov.pagopa.pu.debtpositions.controller.BaseApi;
import it.gov.pagopa.pu.debtpositions.controller.generated.ReceiptNoPiiEntityControllerApi;
import it.gov.pagopa.pu.fileshare.config.rest.RestTemplateConfig;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class DebtPositionsApisHolder {

    private final ReceiptNoPiiEntityControllerApi receiptNoPiiEntityControllerApi;

    private final ThreadLocal<String> bearerTokenHolder = new ThreadLocal<>();

    public DebtPositionsApisHolder(
        DebtPositionsApiClientConfig clientConfig,
        RestTemplateBuilder restTemplateBuilder
    ) {
        RestTemplate restTemplate = restTemplateBuilder.build();
        ApiClient apiClient = new ApiClient(restTemplate);
      apiClient.setBasePath(clientConfig.getBaseUrl());
      apiClient.setBearerToken(bearerTokenHolder::get);
      apiClient.setMaxAttemptsForRetry(Math.max(1, clientConfig.getMaxAttempts()));
      apiClient.setWaitTimeMillis(clientConfig.getWaitTimeMillis());
      if (clientConfig.isPrintBodyWhenError()) {
        restTemplate.setErrorHandler(RestTemplateConfig.bodyPrinterWhenError("DEBT-POSITIONS"));
      }

        this.receiptNoPiiEntityControllerApi = new ReceiptNoPiiEntityControllerApi(apiClient);
    }

    @PreDestroy
    public void unload(){
        bearerTokenHolder.remove();
    }

    public ReceiptNoPiiEntityControllerApi getReceiptNoPiiEntityControllerApi(String accessToken){
        return getApi(accessToken, receiptNoPiiEntityControllerApi);
    }

    private <T extends BaseApi> T getApi(String accessToken, T api) {
        bearerTokenHolder.set(accessToken);
        return api;
    }
}
