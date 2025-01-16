package it.gov.pagopa.pu.fileshare.connector.processexecutions.client;

import it.gov.pagopa.pu.fileshare.connector.processexecutions.config.ProcessExecutionsApisHolder;
import it.gov.pagopa.pu.p4paprocessexecutions.controller.generated.IngestionFlowFileControllerApi;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFileRequestDTO;
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
class IngestionFlowFileClientTest {
  @Mock
  private ProcessExecutionsApisHolder processExecutionsApisHolderMock;
  @Mock
  private IngestionFlowFileControllerApi ingestionFlowFileControllerApiMock;

  private IngestionFlowFileClient ingestionFlowFileClient;

  @BeforeEach
  void setUp() {
    ingestionFlowFileClient = new IngestionFlowFileClient(
      processExecutionsApisHolderMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      processExecutionsApisHolderMock
    );
  }

  @Test
  void whenCreateIngestionFlowFileThenOK() {
    // Given
    String accessToken = "ACCESSTOKEN";
    IngestionFlowFileRequestDTO ingestionFlowFileRequestDTO = new IngestionFlowFileRequestDTO();

    Mockito.when(processExecutionsApisHolderMock.getIngestionFlowFileControllerApi(accessToken))
      .thenReturn(ingestionFlowFileControllerApiMock);
    // When
    ingestionFlowFileClient.createIngestionFlowFile(ingestionFlowFileRequestDTO, accessToken);

    // Then
    Mockito.verify(ingestionFlowFileControllerApiMock).createIngestionFlowFile(ingestionFlowFileRequestDTO);
  }

  @Test
  void givenGenericHttpExceptionWhenCreateIngestionFlowFileThenThrowIt() {
    // Given
    String accessToken = "ACCESSTOKEN";
    HttpClientErrorException expectedException = new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
    IngestionFlowFileRequestDTO ingestionFlowFileRequestDTO = new IngestionFlowFileRequestDTO();

    Mockito.when(processExecutionsApisHolderMock.getIngestionFlowFileControllerApi(accessToken))
      .thenReturn(ingestionFlowFileControllerApiMock);
    Mockito.doThrow(expectedException).when(ingestionFlowFileControllerApiMock).createIngestionFlowFile(ingestionFlowFileRequestDTO);

    // When
    HttpClientErrorException result = Assertions.assertThrows(expectedException.getClass(), () -> ingestionFlowFileClient.createIngestionFlowFile(ingestionFlowFileRequestDTO, accessToken));

    // Then
    Assertions.assertSame(expectedException, result);
  }

  @Test
  void givenGenericExceptionWhenCreateIngestionFlowFileThenThrowIt() {
    // Given
    String accessToken = "ACCESSTOKEN";
    RuntimeException expectedException = new RuntimeException();
    IngestionFlowFileRequestDTO ingestionFlowFileRequestDTO = new IngestionFlowFileRequestDTO();

    Mockito.when(processExecutionsApisHolderMock.getIngestionFlowFileControllerApi(accessToken))
      .thenReturn(ingestionFlowFileControllerApiMock);
    Mockito.doThrow(expectedException).when(ingestionFlowFileControllerApiMock).createIngestionFlowFile(ingestionFlowFileRequestDTO);

    // When
    RuntimeException result = Assertions.assertThrows(expectedException.getClass(), () -> ingestionFlowFileClient.createIngestionFlowFile(ingestionFlowFileRequestDTO, accessToken));

    // Then
    Assertions.assertSame(expectedException, result);
  }
}
