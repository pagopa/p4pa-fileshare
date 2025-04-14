package it.gov.pagopa.pu.fileshare.connector.processexecutions.client;

import it.gov.pagopa.pu.fileshare.connector.processexecutions.config.ProcessExecutionsApisHolder;
import it.gov.pagopa.pu.p4paprocessexecutions.controller.generated.IngestionFlowFileSearchControllerApi;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFile;
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
class IngestionFlowFileSearchClientTest {

  private final String accessToken = "ACCESSTOKEN";

  @Mock
  private ProcessExecutionsApisHolder processExecutionsApisHolderMock;
  @Mock
  private IngestionFlowFileSearchControllerApi ingestionFlowFileSearchControllerApiMock;

  private IngestionFlowFileSearchClient client;

  @BeforeEach
  void init(){
    client = new IngestionFlowFileSearchClient(processExecutionsApisHolderMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(
      processExecutionsApisHolderMock,
      ingestionFlowFileSearchControllerApiMock
    );
  }

  @Test
  void whenGetIngestionFlowFileThenReturnIngestionFlowFile() {
    Long organizationId = 1L;
    String filePathName = "FILE_PATH_NAME";
    String fileName = "FILE_NAME";
    IngestionFlowFile expectedIngestionFlowFile = new IngestionFlowFile();

    Mockito.when(processExecutionsApisHolderMock.getIngestionFlowFileSearchControllerApi(accessToken))
      .thenReturn(ingestionFlowFileSearchControllerApiMock);

    Mockito.when(ingestionFlowFileSearchControllerApiMock.crudIngestionFlowFilesFindByOrganizationIdAndFilePathNameAndFileName(organizationId, filePathName, fileName))
      .thenReturn(expectedIngestionFlowFile);

    IngestionFlowFile result = client.findByOrganizationIdAndFilePathNameAndFileName(organizationId, filePathName, fileName, accessToken);

    Assertions.assertSame(expectedIngestionFlowFile, result);
  }


  @Test
  void givenHttpClientErrorExceptionOtherStatusWhenGetIngestionFlowFileThenThrowIt() {
    Long organizationId = 1L;
    String filePathName = "FILE_PATH_NAME";
    String fileName = "FILE_NAME";

    Mockito.when(processExecutionsApisHolderMock.getIngestionFlowFileSearchControllerApi(accessToken))
      .thenReturn(ingestionFlowFileSearchControllerApiMock);

    Mockito.when(ingestionFlowFileSearchControllerApiMock.crudIngestionFlowFilesFindByOrganizationIdAndFilePathNameAndFileName(organizationId, filePathName, fileName))
      .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "NotFound", null, null, null));

    IngestionFlowFile result = client.findByOrganizationIdAndFilePathNameAndFileName(organizationId, filePathName, fileName, accessToken);

    Assertions.assertNull(result);
  }

}
