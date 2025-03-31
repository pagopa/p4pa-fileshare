package it.gov.pagopa.pu.fileshare.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.core.io.Resource;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileResourceDTO {
  private Resource resourceStream;
  private String fileName;
}
