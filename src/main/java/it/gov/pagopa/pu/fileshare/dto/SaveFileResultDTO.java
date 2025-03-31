package it.gov.pagopa.pu.fileshare.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SaveFileResultDTO {
  private String relativePath;
  private byte[] fileHash;
}
