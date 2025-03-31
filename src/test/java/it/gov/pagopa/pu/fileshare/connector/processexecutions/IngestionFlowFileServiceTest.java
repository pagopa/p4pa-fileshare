package it.gov.pagopa.pu.fileshare.connector.processexecutions;

import it.gov.pagopa.pu.fileshare.connector.processexecutions.client.IngestionFlowFileClient;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFile;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFileRequestDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IngestionFlowFileServiceTest {

  @Mock
  private IngestionFlowFileClient clientMock;

  private IngestionFlowFileService service;

  @BeforeEach
  void init(){
    service = new IngestionFlowFileServiceImpl(clientMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(clientMock);
  }

  @Test
  void whenGetIngestionFlowFileThenInvokeClient(){
    // Given
    Long organizationId = 1L;
    String accessToken = "ACCESSTOKEN";
    IngestionFlowFile expectedResult = new IngestionFlowFile();

    Mockito.when(clientMock.getIngestionFlowFile(Mockito.same(organizationId), Mockito.same(accessToken)))
      .thenReturn(expectedResult);

    // When
    IngestionFlowFile result = service.getIngestionFlowFile(organizationId, accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @Test
  void whenCreateIngestionFlowFileThenInvokeClient(){
    // Given
    IngestionFlowFileRequestDTO requestDTO = new IngestionFlowFileRequestDTO();
    String accessToken = "ACCESSTOKEN";
    Long expectedResult = 2L;

    Mockito.when(clientMock.createIngestionFlowFile(Mockito.same(requestDTO), Mockito.same(accessToken)))
      .thenReturn(expectedResult);

    // When
    Long result = service.createIngestionFlowFile(requestDTO, accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }
}
