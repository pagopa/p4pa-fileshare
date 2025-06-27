package it.gov.pagopa.pu.fileshare.util;

import it.gov.pagopa.pu.fileshare.dto.FileResourceDTO;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public class Utilities {
  private Utilities(){}

  public static ResponseEntity<Resource> buildResourceResponseEntity(FileResourceDTO fileResourceDTO) {
    Resource fileResource = new InputStreamResource(
      fileResourceDTO.getResourceStream());

    HttpHeaders headers = new HttpHeaders();
    headers.setContentDisposition(ContentDisposition.attachment()
      .filename(fileResourceDTO.getFileName())
      .build());

    return ResponseEntity.ok()
      .contentType(MediaType.APPLICATION_OCTET_STREAM)
      .headers(headers)
      .body(fileResource);
  }
}
