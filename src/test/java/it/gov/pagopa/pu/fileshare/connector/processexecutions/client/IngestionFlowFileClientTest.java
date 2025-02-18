package it.gov.pagopa.pu.fileshare.connector.processexecutions.client;

import it.gov.pagopa.pu.fileshare.connector.processexecutions.config.ProcessExecutionsApisHolder;
import it.gov.pagopa.pu.p4paprocessexecutions.controller.generated.IngestionFlowFileControllerApi;
import it.gov.pagopa.pu.p4paprocessexecutions.controller.generated.IngestionFlowFileEntityControllerApi;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import java.net.URI;

@ExtendWith(MockitoExtension.class)
class IngestionFlowFileClientTest {

  private final String accessToken = "ACCESSTOKEN";

  @Mock
  private ProcessExecutionsApisHolder processExecutionsApisHolderMock;
  @Mock
  private IngestionFlowFileControllerApi ingestionFlowFileControllerApiMock;
  @Mock
  private IngestionFlowFileEntityControllerApi ingestionFlowFileEntityControllerApiMock;

  private IngestionFlowFileClient client;

  @BeforeEach
  void init(){
    client = new IngestionFlowFileClient(processExecutionsApisHolderMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(
      processExecutionsApisHolderMock,
      ingestionFlowFileControllerApiMock,
      ingestionFlowFileEntityControllerApiMock
    );
  }

  @Test
  void whenCreateIngestionFlowFileThenOK() {
    IngestionFlowFileRequestDTO ingestionFlowFileRequestDTO = new IngestionFlowFileRequestDTO();
    Long expectedIngestionFlowFileId = 1L;

    Mockito.when(processExecutionsApisHolderMock.getIngestionFlowFileControllerApi(accessToken))
      .thenReturn(ingestionFlowFileControllerApiMock);
    Mockito.when(ingestionFlowFileControllerApiMock.createIngestionFlowFileWithHttpInfo(ingestionFlowFileRequestDTO))
      .thenReturn(ResponseEntity.created(URI.create(String.valueOf(expectedIngestionFlowFileId))).build());

    Long result = client.createIngestionFlowFile(ingestionFlowFileRequestDTO, accessToken);

    Assertions.assertSame(expectedIngestionFlowFileId, result);
  }

  @Test
  void whenGetIngestionFlowFileThenReturnIngestionFlowFile() {
    Long ingestionFlowFileId = 123L;
    IngestionFlowFile expectedIngestionFlowFile = new IngestionFlowFile();

    Mockito.when(processExecutionsApisHolderMock.getIngestionFlowFileEntityControllerApi(accessToken))
      .thenReturn(ingestionFlowFileEntityControllerApiMock);

    Mockito.when(ingestionFlowFileEntityControllerApiMock.crudGetIngestionflowfile(ingestionFlowFileId+""))
      .thenReturn(expectedIngestionFlowFile);

    IngestionFlowFile result = client.getIngestionFlowFile(ingestionFlowFileId, accessToken);

    Assertions.assertSame(expectedIngestionFlowFile, result);
  }


  @Test
  void givenHttpClientErrorExceptionOtherStatusWhenGetIngestionFlowFileThenThrowIt() {
    Long ingestionFlowFileId = 123L;

    Mockito.when(processExecutionsApisHolderMock.getIngestionFlowFileEntityControllerApi(accessToken))
      .thenReturn(ingestionFlowFileEntityControllerApiMock);

    Mockito.when(ingestionFlowFileEntityControllerApiMock.crudGetIngestionflowfile(ingestionFlowFileId+""))
      .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "NotFound", null, null, null));

    IngestionFlowFile result = client.getIngestionFlowFile(ingestionFlowFileId, accessToken);

    Assertions.assertNull(result);
  }

}
