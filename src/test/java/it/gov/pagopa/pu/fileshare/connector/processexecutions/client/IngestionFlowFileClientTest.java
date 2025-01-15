package it.gov.pagopa.pu.fileshare.connector.processexecutions.client;

import it.gov.pagopa.pu.fileshare.connector.processexecutions.config.ProcessExecutionsApisHolder;
import it.gov.pagopa.pu.p4paprocessexecutions.controller.generated.IngestionFlowFileControllerApi;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFileDTO;
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
    IngestionFlowFileDTO expectedResult = new IngestionFlowFileDTO();

    Mockito.when(processExecutionsApisHolderMock.getIngestionFlowFileControllerApi(accessToken))
      .thenReturn(ingestionFlowFileControllerApiMock);
    Mockito.when(ingestionFlowFileControllerApiMock.createIngestionFlowFile(expectedResult))
      .thenReturn(expectedResult);

    // When
    IngestionFlowFileDTO result = ingestionFlowFileClient.createIngestionFlowFile(expectedResult, accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @Test
  void givenGenericHttpExceptionWhenCreateIngestionFlowFileThenThrowIt() {
    // Given
    String accessToken = "ACCESSTOKEN";
    HttpClientErrorException expectedException = new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
    IngestionFlowFileDTO ingestionFlowFileDTO = new IngestionFlowFileDTO();

    Mockito.when(processExecutionsApisHolderMock.getIngestionFlowFileControllerApi(accessToken))
      .thenReturn(ingestionFlowFileControllerApiMock);
    Mockito.when(ingestionFlowFileControllerApiMock.createIngestionFlowFile(ingestionFlowFileDTO))
      .thenThrow(expectedException);

    // When
    HttpClientErrorException result = Assertions.assertThrows(expectedException.getClass(), () -> ingestionFlowFileClient.createIngestionFlowFile(ingestionFlowFileDTO, accessToken));

    // Then
    Assertions.assertSame(expectedException, result);
  }

  @Test
  void givenGenericExceptionWhenCreateIngestionFlowFileThenThrowIt() {
    // Given
    String accessToken = "ACCESSTOKEN";
    RuntimeException expectedException = new RuntimeException();
    IngestionFlowFileDTO ingestionFlowFileDTO = new IngestionFlowFileDTO();

    Mockito.when(processExecutionsApisHolderMock.getIngestionFlowFileControllerApi(accessToken))
      .thenReturn(ingestionFlowFileControllerApiMock);
    Mockito.when(ingestionFlowFileControllerApiMock.createIngestionFlowFile(ingestionFlowFileDTO))
      .thenThrow(expectedException);

    // When
    RuntimeException result = Assertions.assertThrows(expectedException.getClass(), () -> ingestionFlowFileClient.createIngestionFlowFile(ingestionFlowFileDTO, accessToken));

    // Then
    Assertions.assertSame(expectedException, result);
  }
}
