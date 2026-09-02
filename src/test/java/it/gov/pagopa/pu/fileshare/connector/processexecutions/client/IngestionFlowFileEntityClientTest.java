package it.gov.pagopa.pu.fileshare.connector.processexecutions.client;

import it.gov.pagopa.pu.fileshare.connector.processexecutions.config.ProcessExecutionsApisHolder;
import it.gov.pagopa.pu.fileshare.exception.common.RestInvokeNotFoundException;
import it.gov.pagopa.pu.processexecutions.client.generated.IngestionFlowFileControllerApi;
import it.gov.pagopa.pu.processexecutions.client.generated.IngestionFlowFileEntityControllerApi;
import it.gov.pagopa.pu.processexecutions.dto.generated.IngestionFlowFile;
import it.gov.pagopa.pu.processexecutions.dto.generated.IngestionFlowFileRequestDTO;
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

import java.net.URI;
import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IngestionFlowFileEntityClientTest {

  private final String accessToken = "ACCESSTOKEN";

  @Mock
  private ProcessExecutionsApisHolder processExecutionsApisHolderMock;
  @Mock
  private IngestionFlowFileControllerApi ingestionFlowFileControllerApiMock;
  @Mock
  private IngestionFlowFileEntityControllerApi ingestionFlowFileEntityControllerApiMock;

  private IngestionFlowFileEntityClient client;

  @BeforeEach
  void init(){
    client = new IngestionFlowFileEntityClient(processExecutionsApisHolderMock);
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

    when(processExecutionsApisHolderMock.getIngestionFlowFileControllerApi(accessToken))
      .thenReturn(ingestionFlowFileControllerApiMock);
    when(ingestionFlowFileControllerApiMock.createIngestionFlowFileWithHttpInfo(ingestionFlowFileRequestDTO))
      .thenReturn(ResponseEntity.created(URI.create(String.valueOf(expectedIngestionFlowFileId))).build());

    Long result = client.createIngestionFlowFile(ingestionFlowFileRequestDTO, accessToken);

    Assertions.assertSame(expectedIngestionFlowFileId, result);
  }

  @Test
  void whenGetIngestionFlowFileThenReturnIngestionFlowFile() {
    Long ingestionFlowFileId = 123L;
    IngestionFlowFile expectedIngestionFlowFile = new IngestionFlowFile();

    when(processExecutionsApisHolderMock.getIngestionFlowFileEntityControllerApi(accessToken))
      .thenReturn(ingestionFlowFileEntityControllerApiMock);

    when(ingestionFlowFileEntityControllerApiMock.crudGetIngestionflowfile(ingestionFlowFileId+""))
      .thenReturn(expectedIngestionFlowFile);

    IngestionFlowFile result = client.getIngestionFlowFile(ingestionFlowFileId, accessToken);

    Assertions.assertSame(expectedIngestionFlowFile, result);
  }


  @Test
  void givenHttpClientErrorExceptionOtherStatusWhenGetIngestionFlowFileThenThrowIt() {
    Long ingestionFlowFileId = 123L;

    when(processExecutionsApisHolderMock.getIngestionFlowFileEntityControllerApi(accessToken))
      .thenReturn(ingestionFlowFileEntityControllerApiMock);

    when(ingestionFlowFileEntityControllerApiMock.crudGetIngestionflowfile(ingestionFlowFileId+""))
      .thenThrow(new RestInvokeNotFoundException("APPNAME", HttpStatus.NOT_FOUND, "ERROR", "ERRORCODE", "ERRORMESSAGE"));

    IngestionFlowFile result = client.getIngestionFlowFile(ingestionFlowFileId, accessToken);

    Assertions.assertNull(result);
  }

  @Test
  void whenGetIngestionFlowFileVersionThenOK() {
    List<String> versionList = List.of("1.0", "1.1", "1.3", "1.4", "2.0");

    when(processExecutionsApisHolderMock.getIngestionFlowFileControllerApi(accessToken))
      .thenReturn(ingestionFlowFileControllerApiMock);
    when(ingestionFlowFileControllerApiMock.getIngestionFlowFileVersion(IngestionFlowFile.IngestionFlowFileTypeEnum.DP_INSTALLMENTS.getValue()))
      .thenReturn(versionList);

    List<String> result = client.getIngestionFlowFileVersion(IngestionFlowFileRequestDTO.IngestionFlowFileTypeEnum.DP_INSTALLMENTS, accessToken);

    Assertions.assertSame(versionList, result);
  }
}
