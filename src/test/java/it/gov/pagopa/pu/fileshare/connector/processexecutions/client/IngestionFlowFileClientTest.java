package it.gov.pagopa.pu.fileshare.connector.processexecutions.client;

import it.gov.pagopa.pu.fileshare.connector.processexecutions.config.ProcessExecutionsApisHolder;
import it.gov.pagopa.pu.p4paprocessexecutions.controller.generated.IngestionFlowFileControllerApi;
import it.gov.pagopa.pu.p4paprocessexecutions.controller.generated.IngestionFlowFileEntityControllerApi;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFile;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFileRequestDTO;
import org.junit.jupiter.api.Assertions;
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

  @Mock
  private IngestionFlowFileEntityControllerApi ingestionFlowFileEntityControllerApiMock;

  @Test
  void whenCreateIngestionFlowFileThenOK() {
    ProcessExecutionsApisHolder processExecutionsApisHolderMock = Mockito.mock(ProcessExecutionsApisHolder.class);
    Mockito.when(processExecutionsApisHolderMock.getIngestionFlowFileControllerApi(accessToken))
      .thenReturn(ingestionFlowFileControllerApiMock);

    IngestionFlowFileClient ingestionFlowFileClient = new IngestionFlowFileClient(processExecutionsApisHolderMock);

    IngestionFlowFileRequestDTO ingestionFlowFileRequestDTO = new IngestionFlowFileRequestDTO();
    String expectedIngestionFlowFileId = "INGESTIONFLOWFILEID";
    Mockito.when(ingestionFlowFileControllerApiMock.createIngestionFlowFileWithHttpInfo(ingestionFlowFileRequestDTO))
      .thenReturn(ResponseEntity.created(URI.create(expectedIngestionFlowFileId)).build());

    String result = ingestionFlowFileClient.createIngestionFlowFile(ingestionFlowFileRequestDTO, accessToken);

    Assertions.assertSame(expectedIngestionFlowFileId, result);

    Mockito.verifyNoMoreInteractions(ingestionFlowFileControllerApiMock);
  }

  @Test
  void givenGenericHttpExceptionWhenCreateIngestionFlowFileThenThrowIt() {
    ProcessExecutionsApisHolder processExecutionsApisHolderMock = Mockito.mock(ProcessExecutionsApisHolder.class);
    Mockito.when(processExecutionsApisHolderMock.getIngestionFlowFileControllerApi(accessToken))
      .thenReturn(ingestionFlowFileControllerApiMock);

    IngestionFlowFileClient ingestionFlowFileClient = new IngestionFlowFileClient(processExecutionsApisHolderMock);

    HttpClientErrorException expectedException = new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
    IngestionFlowFileRequestDTO ingestionFlowFileRequestDTO = new IngestionFlowFileRequestDTO();
    Mockito.doThrow(expectedException).when(ingestionFlowFileControllerApiMock).createIngestionFlowFileWithHttpInfo(ingestionFlowFileRequestDTO);

    HttpClientErrorException result = Assertions.assertThrows(expectedException.getClass(),
      () -> ingestionFlowFileClient.createIngestionFlowFile(ingestionFlowFileRequestDTO, accessToken));

    Assertions.assertSame(expectedException, result);

    Mockito.verifyNoMoreInteractions(ingestionFlowFileControllerApiMock);
  }

  @Test
  void givenGenericExceptionWhenCreateIngestionFlowFileThenThrowIt() {
    ProcessExecutionsApisHolder processExecutionsApisHolderMock = Mockito.mock(ProcessExecutionsApisHolder.class);
    Mockito.when(processExecutionsApisHolderMock.getIngestionFlowFileControllerApi(accessToken))
      .thenReturn(ingestionFlowFileControllerApiMock);

    IngestionFlowFileClient ingestionFlowFileClient = new IngestionFlowFileClient(processExecutionsApisHolderMock);

    RuntimeException expectedException = new RuntimeException();
    IngestionFlowFileRequestDTO ingestionFlowFileRequestDTO = new IngestionFlowFileRequestDTO();
    Mockito.doThrow(expectedException).when(ingestionFlowFileControllerApiMock).createIngestionFlowFileWithHttpInfo(ingestionFlowFileRequestDTO);

    RuntimeException result = Assertions.assertThrows(expectedException.getClass(),
      () -> ingestionFlowFileClient.createIngestionFlowFile(ingestionFlowFileRequestDTO, accessToken));

    Assertions.assertSame(expectedException, result);

    Mockito.verifyNoMoreInteractions(ingestionFlowFileControllerApiMock);
  }

  @Test
  void whenGetIngestionFlowFileThenReturnIngestionFlowFile() {
    ProcessExecutionsApisHolder processExecutionsApisHolderMock = Mockito.mock(ProcessExecutionsApisHolder.class);
    Mockito.when(processExecutionsApisHolderMock.getIngestionFlowFileEntityControllerApi(accessToken))
      .thenReturn(ingestionFlowFileEntityControllerApiMock);

    IngestionFlowFileClient ingestionFlowFileClient = new IngestionFlowFileClient(processExecutionsApisHolderMock);

    Long ingestionFlowFileId = 123L;
    IngestionFlowFile expectedIngestionFlowFile = new IngestionFlowFile();
    Mockito.when(ingestionFlowFileEntityControllerApiMock.crudGetIngestionflowfile("123"))
      .thenReturn(expectedIngestionFlowFile);

    IngestionFlowFile result = ingestionFlowFileClient.getIngestionFlowFile(ingestionFlowFileId, accessToken);

    Assertions.assertSame(expectedIngestionFlowFile, result);

    Mockito.verifyNoMoreInteractions(ingestionFlowFileEntityControllerApiMock);
  }

  @Test
  void givenGenericExceptionWhenGetIngestionFlowFileThenThrowIt() {
    ProcessExecutionsApisHolder processExecutionsApisHolderMock = Mockito.mock(ProcessExecutionsApisHolder.class);
    Mockito.when(processExecutionsApisHolderMock.getIngestionFlowFileEntityControllerApi(accessToken))
      .thenReturn(ingestionFlowFileEntityControllerApiMock);

    IngestionFlowFileClient ingestionFlowFileClient = new IngestionFlowFileClient(processExecutionsApisHolderMock);

    Long ingestionFlowFileId = 123L;
    RuntimeException genericException = new RuntimeException("Unexpected error");
    Mockito.when(ingestionFlowFileEntityControllerApiMock.crudGetIngestionflowfile("123"))
      .thenThrow(genericException);

    RuntimeException result = Assertions.assertThrows(RuntimeException.class,
      () -> ingestionFlowFileClient.getIngestionFlowFile(ingestionFlowFileId, accessToken));

    Assertions.assertSame(genericException, result);
  }

  @Test
  void givenHttpClientErrorExceptionOtherStatusWhenGetIngestionFlowFileThenThrowIt() {
    ProcessExecutionsApisHolder processExecutionsApisHolderMock = Mockito.mock(ProcessExecutionsApisHolder.class);
    Mockito.when(processExecutionsApisHolderMock.getIngestionFlowFileEntityControllerApi(accessToken))
      .thenReturn(ingestionFlowFileEntityControllerApiMock);

    IngestionFlowFileClient ingestionFlowFileClient = new IngestionFlowFileClient(processExecutionsApisHolderMock);

    Long ingestionFlowFileId = 123L;
    HttpClientErrorException genericException = new HttpClientErrorException(HttpStatus.BAD_REQUEST);
    Mockito.when(ingestionFlowFileEntityControllerApiMock.crudGetIngestionflowfile("123"))
      .thenThrow(genericException);

    HttpClientErrorException result = Assertions.assertThrows(HttpClientErrorException.class,
      () -> ingestionFlowFileClient.getIngestionFlowFile(ingestionFlowFileId, accessToken));

    Assertions.assertSame(genericException, result);
  }

}
