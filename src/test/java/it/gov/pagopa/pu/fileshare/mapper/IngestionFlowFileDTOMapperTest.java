package it.gov.pagopa.pu.fileshare.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import it.gov.pagopa.pu.fileshare.dto.generated.FileOrigin;
import it.gov.pagopa.pu.fileshare.dto.generated.IngestionFlowFileType;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFileRequestDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class IngestionFlowFileDTOMapperTest {
  private final IngestionFlowFileDTOMapper mapper = new IngestionFlowFileDTOMapper();

  @Test
  void whenMapToIngestionFlowFileDTOThenOK(){
    String filePath = "/path";
    Long organizationId = 123L;
    MockMultipartFile file = new MockMultipartFile(
      "ingestionFlowFile",
      "test.txt",
      MediaType.TEXT_PLAIN_VALUE,
      "this is a test file".getBytes()
    );

    IngestionFlowFileRequestDTO result = mapper.mapToIngestionFlowFileDTO(file,
      IngestionFlowFileType.RECEIPT, FileOrigin.PAGOPA, organizationId,filePath);

    Assertions.assertNotNull(result);
    assertEquals(organizationId, result.getOrganizationId());
    assertEquals(filePath, result.getFilePathName());
    assertEquals(file.getOriginalFilename(), result.getFileName());
    assertEquals(file.getSize(), result.getFileSize());
    assertEquals(IngestionFlowFileRequestDTO.FlowFileTypeEnum.RECEIPT, result.getFlowFileType());
    assertEquals(FileOrigin.PAGOPA.toString(), result.getFileOrigin());
  }
}
