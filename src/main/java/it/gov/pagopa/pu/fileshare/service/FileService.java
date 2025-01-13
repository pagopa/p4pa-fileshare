package it.gov.pagopa.pu.fileshare.service;

import it.gov.pagopa.pu.fileshare.exception.custom.InvalidFileException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class FileService {
  public void validateFile(MultipartFile ingestionFlowFile, String validFileExt) {
    if( ingestionFlowFile == null || !StringUtils.defaultString(ingestionFlowFile.getOriginalFilename()).endsWith(validFileExt)){
      log.debug("Invalid ingestion flow file extension");
      throw new InvalidFileException("Invalid file extension");
    }
  }
}
