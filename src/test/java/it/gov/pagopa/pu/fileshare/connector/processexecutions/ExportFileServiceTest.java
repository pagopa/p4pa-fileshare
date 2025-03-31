package it.gov.pagopa.pu.fileshare.connector.processexecutions;

import it.gov.pagopa.pu.fileshare.connector.processexecutions.client.ExportFileClient;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.ExportFile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExportFileServiceTest {

  @Mock
  private ExportFileClient clientMock;

  private ExportFileService service;

  @BeforeEach
  void init(){
    service = new ExportFileServiceImpl(clientMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(clientMock);
  }

  @Test
  void whenGetExportFileThenInvokeClient(){
    // Given
    String accessToken = "ACCESSTOKEN";
    ExportFile expectedResult = new ExportFile();

    Mockito.when(clientMock.getExportFile(Mockito.eq(1L), Mockito.same(accessToken)))
      .thenReturn(expectedResult);

    // When
    ExportFile result = service.getExportFile(1L, accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }
}
