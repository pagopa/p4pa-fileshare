package it.gov.pagopa.pu.fileshare.util;

import it.gov.pagopa.pu.fileshare.dto.FileResourceDTO;
import it.gov.pagopa.pu.fileshare.exception.custom.InvalidFileException;
import org.slf4j.MDC;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

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

  public static String getTraceId(){
    return MDC.get("traceId");
  }

  public static String getSpanId(){
    return MDC.get("spanId");
  }

  public static MultipartFile getExclusivePresenceOrThrow(MultipartFile a, MultipartFile b) {
    if (a != null && b == null) return a;
    if (a == null && b != null) return b;
    throw new InvalidFileException("INVALID_FILES", "Exactly one of the two files must be non-null");
  }
}
