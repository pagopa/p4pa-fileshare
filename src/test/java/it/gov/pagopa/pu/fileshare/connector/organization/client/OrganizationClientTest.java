package it.gov.pagopa.pu.fileshare.connector.organization.client;

import it.gov.pagopa.pu.fileshare.connector.organization.config.OrganizationApisHolder;
import it.gov.pagopa.pu.p4paorganization.controller.generated.OrganizationEntityControllerApi;
import it.gov.pagopa.pu.p4paorganization.dto.generated.Organization;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

@ExtendWith(MockitoExtension.class)
class OrganizationClientTest {
  @Mock
  private OrganizationApisHolder organizationApisHolder;
  @Mock
  private OrganizationEntityControllerApi organizationEntityControllerApiMock;

  private OrganizationClient organizationClient;

  @BeforeEach
  void setUp() {
    organizationClient = new OrganizationClient(organizationApisHolder);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      organizationApisHolder
    );
  }

  @Test
  void whenGetOrganizationByIdThenInvokeWithAccessToken() {
    // Given
    long organizationId = 1L;
    String accessToken = "ACCESSTOKEN";
    Organization expectedResult = new Organization();

    Mockito.when(organizationApisHolder.getOrganizationEntityControllerApi(accessToken))
      .thenReturn(organizationEntityControllerApiMock);
    Mockito.when(organizationEntityControllerApiMock.crudGetOrganization(String.valueOf(organizationId)))
      .thenReturn(expectedResult);

    // When
    Organization result = organizationClient.getOrganizationById(organizationId, accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @Test
  void givenNoExistentOrganizationIdWhenGetOrganizationByIdThenNull() {
    // Given
    long organizationId = 1L;
    String accessToken = "ACCESSTOKEN";

    Mockito.when(organizationApisHolder.getOrganizationEntityControllerApi(accessToken))
      .thenReturn(organizationEntityControllerApiMock);
    Mockito.when(organizationEntityControllerApiMock.crudGetOrganization(String.valueOf(organizationId)))
      .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

    // When
    Organization result = organizationClient.getOrganizationById(organizationId, accessToken);

    // Then
    Assertions.assertNull(result);
  }

  @Test
  void givenGenericHttpExceptionWhenGetOrganizationByIdThenThrowIt() {
    // Given
    long organizationId = 1L;
    String accessToken = "ACCESSTOKEN";
    HttpClientErrorException expectedException = new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR);

    Mockito.when(organizationApisHolder.getOrganizationEntityControllerApi(accessToken))
      .thenReturn(organizationEntityControllerApiMock);
    Mockito.when(organizationEntityControllerApiMock.crudGetOrganization(String.valueOf(organizationId)))
      .thenThrow(expectedException);

    // When
    HttpClientErrorException result = Assertions.assertThrows(expectedException.getClass(), () -> organizationClient.getOrganizationById(organizationId, accessToken));

    // Then
    Assertions.assertSame(expectedException, result);
  }

  @Test
  void givenGenericExceptionWhenGetOrganizationByIdThenThrowIt() {
    // Given
    long organizationId = 1L;
    String accessToken = "ACCESSTOKEN";
    RuntimeException expectedException = new RuntimeException();

    Mockito.when(organizationApisHolder.getOrganizationEntityControllerApi(accessToken))
      .thenReturn(organizationEntityControllerApiMock);
    Mockito.when(organizationEntityControllerApiMock.crudGetOrganization(String.valueOf(organizationId)))
      .thenThrow(expectedException);

    // When
    RuntimeException result = Assertions.assertThrows(expectedException.getClass(), () -> organizationClient.getOrganizationById(organizationId, accessToken));

    // Then
    Assertions.assertSame(expectedException, result);
  }
}
