package it.gov.pagopa.pu.fileshare.connector.processexecutions.config;

import it.gov.pagopa.pu.fileshare.config.rest.RestTemplateConfig;
import it.gov.pagopa.pu.p4paprocessexecutions.controller.ApiClient;
import it.gov.pagopa.pu.p4paprocessexecutions.controller.BaseApi;
import it.gov.pagopa.pu.p4paprocessexecutions.controller.generated.*;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ProcessExecutionsApisHolder {

  private final IngestionFlowFileControllerApi ingestionFlowFileControllerApi;
  private final IngestionFlowFileEntityControllerApi ingestionFlowFileEntityControllerApi;
  private final IngestionFlowFileEntityExtendedControllerApi ingestionFlowFileEntityExtendedControllerApi;
  private final IngestionFlowFileSearchControllerApi ingestionFlowFileSearchControllerApi;
  private final ExportFileEntityControllerApi exportFileEntityControllerApi;

  private final ThreadLocal<String> bearerTokenHolder = new ThreadLocal<>();

  public ProcessExecutionsApisHolder(
    ProcessExecutionsApiClientConfig clientConfig,
    RestTemplateBuilder restTemplateBuilder
  ) {
    RestTemplate restTemplate = restTemplateBuilder.build();
    ApiClient apiClient = new ApiClient(restTemplate);
    apiClient.setBasePath(clientConfig.getBaseUrl());
    apiClient.setBearerToken(bearerTokenHolder::get);
    apiClient.setMaxAttemptsForRetry(Math.max(1, clientConfig.getMaxAttempts()));
    apiClient.setWaitTimeMillis(clientConfig.getWaitTimeMillis());
    if (clientConfig.isPrintBodyWhenError()) {
      restTemplate.setErrorHandler(RestTemplateConfig.bodyPrinterWhenError("PROCESS-EXECUTIONS"));
    }

    this.ingestionFlowFileControllerApi = new IngestionFlowFileControllerApi(apiClient);
    this.ingestionFlowFileEntityControllerApi = new IngestionFlowFileEntityControllerApi(apiClient);
    this.ingestionFlowFileEntityExtendedControllerApi = new IngestionFlowFileEntityExtendedControllerApi(apiClient);
    this.ingestionFlowFileSearchControllerApi = new IngestionFlowFileSearchControllerApi(apiClient);
    this.exportFileEntityControllerApi = new ExportFileEntityControllerApi(apiClient);
  }

  @PreDestroy
  public void unload() {
    bearerTokenHolder.remove();
  }

  /**
   * It will return a {@link IngestionFlowFileControllerApi} instrumented with
   * the provided accessToken. Use null if auth is not required
   */
  public IngestionFlowFileControllerApi getIngestionFlowFileControllerApi(
    String accessToken) {
    return getApi(accessToken, ingestionFlowFileControllerApi);
  }

  public IngestionFlowFileEntityControllerApi getIngestionFlowFileEntityControllerApi(
    String accessToken) {
    return getApi(accessToken, ingestionFlowFileEntityControllerApi);
  }

  public IngestionFlowFileEntityExtendedControllerApi getIngestionFlowFileEntityExtendedControllerApi(
    String accessToken) {
    return getApi(accessToken, ingestionFlowFileEntityExtendedControllerApi);
  }

  public IngestionFlowFileSearchControllerApi getIngestionFlowFileSearchControllerApi(
    String accessToken) {
    return getApi(accessToken, ingestionFlowFileSearchControllerApi);
  }

  public ExportFileEntityControllerApi getExportFileEntityControllerApi(
    String accessToken) {
    return getApi(accessToken, exportFileEntityControllerApi);
  }

  private <T extends BaseApi> T getApi(String accessToken, T api) {
    bearerTokenHolder.set(accessToken);
    return api;
  }
}
