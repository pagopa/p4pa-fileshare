package it.gov.pagopa.pu.fileshare.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFileDTO;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFileDTO.StatusEnum;
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

    IngestionFlowFileDTO result = mapper.mapToIngestionFlowFileDTO(file,organizationId,filePath);

    Assertions.assertNotNull(result);
    assertEquals(organizationId, result.getOrganizationId());
    assertEquals(StatusEnum.UPLOADED, result.getStatus());
    assertEquals(filePath, result.getFilePath());
    assertEquals(file.getOriginalFilename(), result.getFileName());
    assertEquals(file.getSize(), result.getFileSize());
  }
}
