package it.gov.pagopa.pu.fileshare.connector.processexecutions.client;

import it.gov.pagopa.pu.fileshare.connector.processexecutions.config.ProcessExecutionsApisHolder;
import it.gov.pagopa.pu.fileshare.exception.common.RestInvokeNotFoundException;
import it.gov.pagopa.pu.processexecutions.client.generated.ExportFileEntityControllerApi;
import it.gov.pagopa.pu.processexecutions.dto.generated.ExportFile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExportFileEntityClientTest {

  private final String accessToken = "ACCESSTOKEN";

  @Mock
  private ProcessExecutionsApisHolder processExecutionsApisHolderMock;
  @Mock
  private ExportFileEntityControllerApi exportFileEntityControllerApiMock;

  private ExportFileEntityClient client;

  @BeforeEach
  void init(){
    client = new ExportFileEntityClient(processExecutionsApisHolderMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(
      processExecutionsApisHolderMock,
      exportFileEntityControllerApiMock
    );
  }

  @Test
  void whenGetIngestionFlowFileThenReturnIngestionFlowFile() {
    Long exportFileId = 123L;
    ExportFile expectedExportFile = new ExportFile();

    when(processExecutionsApisHolderMock.getExportFileEntityControllerApi(accessToken))
      .thenReturn(exportFileEntityControllerApiMock);

    when(exportFileEntityControllerApiMock.crudGetExportfile(exportFileId+""))
      .thenReturn(expectedExportFile);

    ExportFile result = client.getExportFile(exportFileId, accessToken);

    Assertions.assertSame(expectedExportFile, result);
  }


  @Test
  void givenHttpClientErrorExceptionOtherStatusWhenGetIngestionFlowFileThenThrowIt() {
    Long exportFileId = 123L;

    when(processExecutionsApisHolderMock.getExportFileEntityControllerApi(accessToken))
      .thenReturn(exportFileEntityControllerApiMock);

    when(exportFileEntityControllerApiMock.crudGetExportfile(exportFileId+""))
      .thenThrow(new RestInvokeNotFoundException("APPNAME", HttpStatus.NOT_FOUND, "ERROR", "ERRORCODE", "ERRORMESSAGE"));

    ExportFile result = client.getExportFile(exportFileId, accessToken);

    Assertions.assertNull(result);
  }

}
