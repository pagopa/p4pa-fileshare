package it.gov.pagopa.pu.fileshare.config;

import it.gov.pagopa.pu.fileshare.dto.generated.IngestionFlowFileType;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "folders")
public class FoldersPathsConfig {
  private String shared;
  private Map<IngestionFlowFileType,String> ingestionFlowFileTypePaths;

  public String getIngestionFlowFilePath(IngestionFlowFileType ingestionFlowFileType) {
    return Optional.ofNullable(
        ingestionFlowFileTypePaths.get(ingestionFlowFileType))
      .orElseThrow(()-> {
        log.debug("No path configured for ingestionFlowFileType {}",ingestionFlowFileType);
        return new UnsupportedOperationException();
      });
  }
}
