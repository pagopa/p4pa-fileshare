package it.gov.pagopa.pu.fileshare.config;

import it.gov.pagopa.pu.fileshare.dto.generated.IngestionFlowFileType;
import java.util.EnumMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FoldersPathsConfigTest {
  private FoldersPathsConfig foldersPathsConfig;

  @BeforeEach
  void setUp() {
    foldersPathsConfig = new FoldersPathsConfig();

  }

  @Test
  void givenPopulatedPathWhenGetIngestionFlowFilePathThenOK(){
    String expected = "/receipt";
    Map<IngestionFlowFileType,String> paths = new EnumMap<>(
      IngestionFlowFileType.class);
    paths.put(IngestionFlowFileType.RECEIPT, "/receipt");
    foldersPathsConfig.setIngestionFlowFileTypePaths(paths);

    String result = foldersPathsConfig.getIngestionFlowFilePath(
      IngestionFlowFileType.RECEIPT);

    Assertions.assertEquals(expected,result);
  }

  @Test
  void givenNoPathWhenGetIngestionFlowFilePathThenUnsupportedOperation(){
    foldersPathsConfig.setIngestionFlowFileTypePaths(new EnumMap<>(IngestionFlowFileType.class));
    try {
      foldersPathsConfig.getIngestionFlowFilePath(
        IngestionFlowFileType.RECEIPT);
      Assertions.fail("Expected UnsupportedOperationException");
    }catch (UnsupportedOperationException e){
      //do nothing
    }
  }
}
