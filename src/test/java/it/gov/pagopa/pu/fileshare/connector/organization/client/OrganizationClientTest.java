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
  private OrganizationApisHolder organizationApisHolderMock;
  @Mock
  private OrganizationEntityControllerApi organizationEntityControllerApiMock;

  private OrganizationClient organizationClient;

  @BeforeEach
  void setUp() {
    organizationClient = new OrganizationClient(organizationApisHolderMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      organizationApisHolderMock,
      organizationEntityControllerApiMock
    );
  }

  @Test
  void whenGetOrganizationByIdThenInvokeWithAccessToken() {
    // Given
    long organizationId = 1L;
    String accessToken = "ACCESSTOKEN";
    Organization expectedResult = new Organization();

    Mockito.when(organizationApisHolderMock.getOrganizationEntityControllerApi(accessToken))
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

    Mockito.when(organizationApisHolderMock.getOrganizationEntityControllerApi(accessToken))
      .thenReturn(organizationEntityControllerApiMock);
    Mockito.when(organizationEntityControllerApiMock.crudGetOrganization(String.valueOf(organizationId)))
      .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "NotFound", null, null, null));

    // When
    Organization result = organizationClient.getOrganizationById(organizationId, accessToken);

    // Then
    Assertions.assertNull(result);
  }

}
