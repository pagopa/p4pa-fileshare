package it.gov.pagopa.pu.fileshare.service;

import it.gov.pagopa.pu.fileshare.exception.custom.InvalidFileException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.stream.Stream;

@Slf4j
@Service
public class FileService {

  public void validateFile(MultipartFile ingestionFlowFile) {
    if( ingestionFlowFile == null){
      log.debug("Invalid ingestion flow file");
      throw new InvalidFileException("Invalid file");
    }
    String filename = StringUtils.defaultString(ingestionFlowFile.getOriginalFilename());
    validateFilename(filename);
  }

  public static void validateFilename(String filename) {
    if(Stream.of("..", "\\", "/").anyMatch(filename::contains)){
      log.debug("Invalid ingestion flow filename");
      throw new InvalidFileException("Invalid filename");
    }
  }

}
