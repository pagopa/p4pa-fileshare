package it.gov.pagopa.pu.fileshare.connector.processexecutions;

import it.gov.pagopa.pu.fileshare.connector.processexecutions.client.IngestionFlowFileEntityClient;
import it.gov.pagopa.pu.fileshare.connector.processexecutions.client.IngestionFlowFileEntityExtendedClient;
import it.gov.pagopa.pu.fileshare.connector.processexecutions.client.IngestionFlowFileSearchClient;
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

import java.util.List;

@ExtendWith(MockitoExtension.class)
class IngestionFlowFileServiceTest {

  @Mock
  private IngestionFlowFileEntityClient entityClientMock;
  @Mock
  private IngestionFlowFileEntityExtendedClient entityExtendedClientMock;
  @Mock
  private IngestionFlowFileSearchClient searchClientMock;

  private IngestionFlowFileService service;

  @BeforeEach
  void init(){
    service = new IngestionFlowFileServiceImpl(entityClientMock, entityExtendedClientMock, searchClientMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(
      entityClientMock,
      entityExtendedClientMock,
      searchClientMock);
  }

  @Test
  void whenGetIngestionFlowFileThenInvokeClient(){
    // Given
    Long organizationId = 1L;
    String accessToken = "ACCESSTOKEN";
    IngestionFlowFile expectedResult = new IngestionFlowFile();

    Mockito.when(entityClientMock.getIngestionFlowFile(Mockito.same(organizationId), Mockito.same(accessToken)))
      .thenReturn(expectedResult);

    // When
    IngestionFlowFile result = service.getIngestionFlowFile(organizationId, accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @Test
  void whenCreateIngestionFlowFileThenInvokeClient(){
    // Given
    IngestionFlowFileRequestDTO requestDTO = new IngestionFlowFileRequestDTO();
    String accessToken = "ACCESSTOKEN";
    Long expectedResult = 2L;

    Mockito.when(entityClientMock.createIngestionFlowFile(Mockito.same(requestDTO), Mockito.same(accessToken)))
      .thenReturn(expectedResult);

    // When
    Long result = service.createIngestionFlowFile(requestDTO, accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @Test
  void whenFindByOrganizationIdAndFilePathNameAndFileNameThenInvokeClient(){
    // Given
    Long organizationId = 1L;
    String filePathName = "FILE_PATH_NAME";
    String fileName = "FILE_NAME";

    String accessToken = "ACCESSTOKEN";
    List<IngestionFlowFile> expectedResult = List.of(new IngestionFlowFile());

    Mockito.when(searchClientMock.findByOrganizationIdAndFilePathNameAndFileName(Mockito.same(organizationId), Mockito.same(filePathName), Mockito.same(fileName), Mockito.same(accessToken)))
      .thenReturn(expectedResult);

    // When
    List<IngestionFlowFile> result = service.findByOrganizationIdAndFilePathNameAndFileName(organizationId, filePathName, fileName, accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @Test
  void whenUpdateFileNamesThenInvokeClient(){
    // Given
    Long ingestionFlowFileId = 1L;
    String fileName = "FILE_NAME";
    String discardFileName = "DISCARD_FILE_NAME";

    String accessToken = "ACCESSTOKEN";
    Integer expectedResult = 0;

    Mockito.when(entityExtendedClientMock.updateFileNames(Mockito.same(ingestionFlowFileId), Mockito.same(fileName), Mockito.same(discardFileName), Mockito.same(accessToken)))
      .thenReturn(expectedResult);

    // When
    Integer result = service.updateFileNames(ingestionFlowFileId, fileName, discardFileName, accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @Test
  void whenGetIngestionFlowFileVersionThenInvokeClient(){
    // Given
    String accessToken = "ACCESSTOKEN";
    List<String> versionList = List.of("1.0", "1.1", "1.3", "1.4", "2.0");

    Mockito.when(entityClientMock.getIngestionFlowFileVersion(IngestionFlowFileRequestDTO.IngestionFlowFileTypeEnum.DP_INSTALLMENTS, accessToken))
      .thenReturn(versionList);

    // When
    List<String> result = service.getIngestionFlowFileVersion(IngestionFlowFileRequestDTO.IngestionFlowFileTypeEnum.DP_INSTALLMENTS, accessToken);

    // Then
    Assertions.assertSame(versionList, result);
  }
}
