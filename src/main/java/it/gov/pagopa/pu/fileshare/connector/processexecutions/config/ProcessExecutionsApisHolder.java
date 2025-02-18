package it.gov.pagopa.pu.fileshare.connector.processexecutions.config;

import it.gov.pagopa.pu.p4paprocessexecutions.controller.ApiClient;
import it.gov.pagopa.pu.p4paprocessexecutions.controller.BaseApi;
import it.gov.pagopa.pu.p4paprocessexecutions.controller.generated.ExportFileEntityControllerApi;
import it.gov.pagopa.pu.p4paprocessexecutions.controller.generated.IngestionFlowFileControllerApi;
import it.gov.pagopa.pu.p4paprocessexecutions.controller.generated.IngestionFlowFileEntityControllerApi;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Lazy
@Service
public class ProcessExecutionsApisHolder {

  private final IngestionFlowFileControllerApi ingestionFlowFileControllerApi;

  private final IngestionFlowFileEntityControllerApi ingestionFlowFileEntityControllerApi;

  private final ExportFileEntityControllerApi exportFileEntityControllerApi;

  private final ThreadLocal<String> bearerTokenHolder = new ThreadLocal<>();

  public ProcessExecutionsApisHolder(
    @Value("${rest.process-executions.base-url}") String baseUrl,
    RestTemplateBuilder restTemplateBuilder) {
    RestTemplate restTemplate = restTemplateBuilder.build();
    ApiClient apiClient = new ApiClient(restTemplate);
    apiClient.setBasePath(baseUrl);
    apiClient.setBearerToken(bearerTokenHolder::get);

    this.ingestionFlowFileControllerApi = new IngestionFlowFileControllerApi(
      apiClient);
    this.ingestionFlowFileEntityControllerApi = new IngestionFlowFileEntityControllerApi(
      apiClient);

    this.exportFileEntityControllerApi = new ExportFileEntityControllerApi(
      apiClient);
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

  public ExportFileEntityControllerApi getExportFileEntityControllerApi(
    String accessToken) {
    return getApi(accessToken, exportFileEntityControllerApi);
  }

  private <T extends BaseApi> T getApi(String accessToken, T api) {
    bearerTokenHolder.set(accessToken);
    return api;
  }
}
