package it.gov.pagopa.pu.fileshare.connector.processexecutions.client;

import it.gov.pagopa.pu.fileshare.connector.processexecutions.config.ProcessExecutionsApisHolder;
import it.gov.pagopa.pu.p4paprocessexecutions.controller.generated.ExportFileEntityControllerApi;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.ExportFile;
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
class ExportFileClientTest {

  private final String accessToken = "ACCESSTOKEN";

  @Mock
  private ProcessExecutionsApisHolder processExecutionsApisHolderMock;
  @Mock
  private ExportFileEntityControllerApi exportFileEntityControllerApiMock;

  private ExportFileClient client;

  @BeforeEach
  void init(){
    client = new ExportFileClient(processExecutionsApisHolderMock);
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

    Mockito.when(processExecutionsApisHolderMock.getExportFileEntityControllerApi(accessToken))
      .thenReturn(exportFileEntityControllerApiMock);

    Mockito.when(exportFileEntityControllerApiMock.crudGetExportfile(exportFileId+""))
      .thenReturn(expectedExportFile);

    ExportFile result = client.getExportFile(exportFileId, accessToken);

    Assertions.assertSame(expectedExportFile, result);
  }


  @Test
  void givenHttpClientErrorExceptionOtherStatusWhenGetIngestionFlowFileThenThrowIt() {
    Long exportFileId = 123L;

    Mockito.when(processExecutionsApisHolderMock.getExportFileEntityControllerApi(accessToken))
      .thenReturn(exportFileEntityControllerApiMock);

    Mockito.when(exportFileEntityControllerApiMock.crudGetExportfile(exportFileId+""))
      .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "NotFound", null, null, null));

    ExportFile result = client.getExportFile(exportFileId, accessToken);

    Assertions.assertNull(result);
  }

}
