package it.gov.pagopa.pu.fileshare.connector.processexecutions.client;

import it.gov.pagopa.pu.fileshare.connector.processexecutions.config.ProcessExecutionsApisHolder;
import it.gov.pagopa.pu.processexecutions.client.generated.IngestionFlowFileEntityExtendedControllerApi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IngestionFlowFileEntityExtendedClientTest {

  @Mock
  private ProcessExecutionsApisHolder processExecutionsApisHolderMock;
  @Mock
  private IngestionFlowFileEntityExtendedControllerApi ingestionFlowFileEntityExtendedControllerApiMock;

  private IngestionFlowFileEntityExtendedClient client;

  @BeforeEach
  void init(){
    client = new IngestionFlowFileEntityExtendedClient(processExecutionsApisHolderMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(
      processExecutionsApisHolderMock,
      ingestionFlowFileEntityExtendedControllerApiMock
    );
  }

  @Test
  void whenGetIngestionFlowFileThenReturnIngestionFlowFile() {
    String accessToken = "ACCESSTOKEN";
    Long ingestionFlowFileId = 1L;
    String fileName = "FILE_NAME";
    String discardFileName = "DISCARD_FILE_NAME";
    Integer expectedResult = 0;

    when(processExecutionsApisHolderMock.getIngestionFlowFileEntityExtendedControllerApi(accessToken))
      .thenReturn(ingestionFlowFileEntityExtendedControllerApiMock);

    when(ingestionFlowFileEntityExtendedControllerApiMock.updateFileNames(ingestionFlowFileId, fileName, discardFileName))
      .thenReturn(expectedResult);

    Integer result = client.updateFileNames(ingestionFlowFileId, fileName, discardFileName, accessToken);

    Assertions.assertSame(expectedResult, result);
  }

}
