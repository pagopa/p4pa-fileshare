package it.gov.pagopa.pu.fileshare.connector.processexecutions.client;

import it.gov.pagopa.pu.fileshare.connector.processexecutions.config.ProcessExecutionsApisHolder;
import it.gov.pagopa.pu.processexecutions.client.generated.IngestionFlowFileSearchControllerApi;
import it.gov.pagopa.pu.processexecutions.dto.generated.CollectionModelIngestionFlowFile;
import it.gov.pagopa.pu.processexecutions.dto.generated.IngestionFlowFile;
import it.gov.pagopa.pu.processexecutions.dto.generated.PagedModelIngestionFlowFileEmbedded;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.when;

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
    List<IngestionFlowFile> expectedIngestionFlowFileList = List.of(new IngestionFlowFile());
    CollectionModelIngestionFlowFile collectionModelIngestionFlowFile = new CollectionModelIngestionFlowFile(new PagedModelIngestionFlowFileEmbedded(expectedIngestionFlowFileList), null);

    when(processExecutionsApisHolderMock.getIngestionFlowFileSearchControllerApi(accessToken))
      .thenReturn(ingestionFlowFileSearchControllerApiMock);

    when(ingestionFlowFileSearchControllerApiMock.crudIngestionFlowFilesFindByOrganizationIdAndFilePathNameAndFileName(organizationId, filePathName, fileName))
      .thenReturn(collectionModelIngestionFlowFile);

    List<IngestionFlowFile> result = client.findByOrganizationIdAndFilePathNameAndFileName(organizationId, filePathName, fileName, accessToken);

    Assertions.assertSame(expectedIngestionFlowFileList, result);
  }


  @Test
  void givenNullEmbeddedWhenGetIngestionFlowFileThenReturnEmptyList() {
    Long organizationId = 1L;
    String filePathName = "FILE_PATH_NAME";
    String fileName = "FILE_NAME";

    when(processExecutionsApisHolderMock.getIngestionFlowFileSearchControllerApi(accessToken))
      .thenReturn(ingestionFlowFileSearchControllerApiMock);

    when(ingestionFlowFileSearchControllerApiMock.crudIngestionFlowFilesFindByOrganizationIdAndFilePathNameAndFileName(organizationId, filePathName, fileName))
      .thenReturn(new CollectionModelIngestionFlowFile(null, null));

    List<IngestionFlowFile> result = client.findByOrganizationIdAndFilePathNameAndFileName(organizationId, filePathName, fileName, accessToken);

    Assertions.assertEquals(List.of(), result);
  }

}
