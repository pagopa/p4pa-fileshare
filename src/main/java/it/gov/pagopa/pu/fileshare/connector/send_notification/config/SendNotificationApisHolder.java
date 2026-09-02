package it.gov.pagopa.pu.fileshare.connector.send_notification.config;

import it.gov.pagopa.pu.fileshare.config.rest.HttpClientErrorJsonBodyHandler;
import it.gov.pagopa.pu.fileshare.connector.send_notification.mapper.SendNotificationErrorDTOMapper;
import it.gov.pagopa.pu.sendnotification.generated.ApiClient;
import it.gov.pagopa.pu.sendnotification.generated.BaseApi;
import it.gov.pagopa.pu.sendnotification.client.generated.NotificationApi;
import it.gov.pagopa.pu.sendnotification.dto.generated.SendNotificationErrorDTO;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.json.JsonMapper;

@Service
public class SendNotificationApisHolder {

    private final NotificationApi notificationApi;

    private final ThreadLocal<String> bearerTokenHolder = new ThreadLocal<>();

    public SendNotificationApisHolder(
        SendNotificationApiClientConfig clientConfig,
        RestTemplateBuilder restTemplateBuilder,
        JsonMapper jsonMapper
    ) {
        RestTemplate restTemplate = restTemplateBuilder.build();
        ApiClient apiClient = new ApiClient(restTemplate);
        apiClient.setBasePath(clientConfig.getBaseUrl());
        apiClient.setBearerToken(bearerTokenHolder::get);
        apiClient.setMaxAttemptsForRetry(Math.max(1, clientConfig.getMaxAttempts()));
        apiClient.setWaitTimeMillis(clientConfig.getWaitTimeMillis());
        restTemplate.setErrorHandler(new HttpClientErrorJsonBodyHandler<>(jsonMapper, "SEND-NOTIFICATION", clientConfig.isPrintBodyWhenError(),
          SendNotificationErrorDTO.class, SendNotificationErrorDTOMapper::map)
        );

        this.notificationApi = new NotificationApi(apiClient);
    }

    @PreDestroy
    public void unload(){
        bearerTokenHolder.remove();
    }

    /** It will return a {@link NotificationApi} instrumented with the provided accessToken. Use null if auth is not required */
    public NotificationApi getNotificationApi(String accessToken){
        return getApi(accessToken, notificationApi);
    }

    private <T extends BaseApi> T getApi(String accessToken, T api) {
        bearerTokenHolder.set(accessToken);
        return api;
    }
}
