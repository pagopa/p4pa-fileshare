package it.gov.pagopa.pu.fileshare.enums;

import it.gov.pagopa.pu.fileshare.dto.generated.IngestionFlowFileType;
import it.gov.pagopa.pu.processexecutions.dto.generated.IngestionFlowFileRequestDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class IngestionFlowFileTypeEnumTest {
  @Test
  void testConversion(){
    for (IngestionFlowFileType value : IngestionFlowFileType.values()) {
      Assertions.assertDoesNotThrow(() -> IngestionFlowFileRequestDTO.IngestionFlowFileTypeEnum.valueOf(value.name()));
    }

  }

}
