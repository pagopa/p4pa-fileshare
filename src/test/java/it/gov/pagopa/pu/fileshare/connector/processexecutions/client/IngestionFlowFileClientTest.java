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
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import java.net.URI;

@ExtendWith(MockitoExtension.class)
class IngestionFlowFileClientTest {

  private final String accessToken = "ACCESSTOKEN";

  @Mock
  private IngestionFlowFileControllerApi ingestionFlowFileControllerApiMock;

  private IngestionFlowFileClient ingestionFlowFileClient;

  @BeforeEach
  void setUp() {
    ProcessExecutionsApisHolder processExecutionsApisHolderMock = Mockito.mock(ProcessExecutionsApisHolder.class);
    Mockito.when(processExecutionsApisHolderMock.getIngestionFlowFileControllerApi(accessToken))
      .thenReturn(ingestionFlowFileControllerApiMock);

    ingestionFlowFileClient = new IngestionFlowFileClient(
      processExecutionsApisHolderMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      ingestionFlowFileControllerApiMock
    );
  }

  @Test
  void whenCreateIngestionFlowFileThenOK() {
    // Given
    IngestionFlowFileRequestDTO ingestionFlowFileRequestDTO = new IngestionFlowFileRequestDTO();

    String expectedIngestionFlowFileId = "INGESTIONFLOWFILEID";
    Mockito.when(ingestionFlowFileControllerApiMock.createIngestionFlowFileWithHttpInfo(ingestionFlowFileRequestDTO))
      .thenReturn(ResponseEntity.created(URI.create(expectedIngestionFlowFileId)).build());

    // When
    String result = ingestionFlowFileClient.createIngestionFlowFile(ingestionFlowFileRequestDTO, accessToken);

    // Then
    Assertions.assertSame(expectedIngestionFlowFileId, result);
  }

  @Test
  void givenGenericHttpExceptionWhenCreateIngestionFlowFileThenThrowIt() {
    // Given
    HttpClientErrorException expectedException = new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
    IngestionFlowFileRequestDTO ingestionFlowFileRequestDTO = new IngestionFlowFileRequestDTO();

    Mockito.doThrow(expectedException).when(ingestionFlowFileControllerApiMock).createIngestionFlowFileWithHttpInfo(ingestionFlowFileRequestDTO);

    // When
    HttpClientErrorException result = Assertions.assertThrows(expectedException.getClass(), () -> ingestionFlowFileClient.createIngestionFlowFile(ingestionFlowFileRequestDTO, accessToken));

    // Then
    Assertions.assertSame(expectedException, result);
  }

  @Test
  void givenGenericExceptionWhenCreateIngestionFlowFileThenThrowIt() {
    // Given
    RuntimeException expectedException = new RuntimeException();
    IngestionFlowFileRequestDTO ingestionFlowFileRequestDTO = new IngestionFlowFileRequestDTO();

    Mockito.doThrow(expectedException).when(ingestionFlowFileControllerApiMock).createIngestionFlowFileWithHttpInfo(ingestionFlowFileRequestDTO);

    // When
    RuntimeException result = Assertions.assertThrows(expectedException.getClass(), () -> ingestionFlowFileClient.createIngestionFlowFile(ingestionFlowFileRequestDTO, accessToken));

    // Then
    Assertions.assertSame(expectedException, result);
  }
}
