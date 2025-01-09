package it.gov.pagopa.pu.fileshare.connector.organization.config;

import it.gov.pagopa.pu.p4paorganization.controller.ApiClient;
import it.gov.pagopa.pu.p4paorganization.controller.BaseApi;
import it.gov.pagopa.pu.p4paorganization.controller.generated.OrganizationEntityControllerApi;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Lazy
@Service
public class OrganizationApisHolder {

    private final OrganizationEntityControllerApi organizationEntityControllerApi;

    private final ThreadLocal<String> bearerTokenHolder = new ThreadLocal<>();

    public OrganizationApisHolder(
            @Value("${app.organization.base-url}") String baseUrl,

            RestTemplateBuilder restTemplateBuilder) {
        RestTemplate restTemplate = restTemplateBuilder.build();
        ApiClient apiClient = new ApiClient(restTemplate);
        apiClient.setBasePath(baseUrl);
        apiClient.setBearerToken(bearerTokenHolder::get);

        this.organizationEntityControllerApi = new OrganizationEntityControllerApi(apiClient);
    }

    @PreDestroy
    public void unload(){
        bearerTokenHolder.remove();
    }

    /** It will return a {@link OrganizationEntityControllerApi} instrumented with the provided accessToken. Use null if auth is not required */
    public OrganizationEntityControllerApi getOrganizationEntityControllerApi(String accessToken){
        return getApi(accessToken, organizationEntityControllerApi);
    }

    private <T extends BaseApi> T getApi(String accessToken, T api) {
        bearerTokenHolder.set(accessToken);
        return api;
    }
}
