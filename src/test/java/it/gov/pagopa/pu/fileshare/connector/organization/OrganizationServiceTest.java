package it.gov.pagopa.pu.fileshare.connector.organization;

import it.gov.pagopa.pu.fileshare.connector.organization.client.OrganizationClient;
import it.gov.pagopa.pu.p4paorganization.dto.generated.Organization;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

  @Mock
  private OrganizationClient clientMock;

  private OrganizationService service;

  @BeforeEach
  void init(){
    service = new OrganizationServiceImpl(clientMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(clientMock);
  }

  @Test
  void whenGetOrganizationByIdThenInvokeClient(){
    // Given
    Long organizationId = 1L;
    String accessToken = "ACCESSTOKEN";
    Organization expectedResult = new Organization();

    Mockito.when(clientMock.getOrganizationById(Mockito.same(organizationId), Mockito.same(accessToken)))
      .thenReturn(expectedResult);

    // When
    Organization result = service.getOrganizationById(organizationId, accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }
}
