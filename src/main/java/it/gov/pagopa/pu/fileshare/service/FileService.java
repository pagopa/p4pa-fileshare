package it.gov.pagopa.pu.fileshare.service;

import it.gov.pagopa.pu.fileshare.exception.custom.InvalidFileException;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class FileService {

  public void validateFile(MultipartFile ingestionFlowFile, String validFileExt) {
    if( ingestionFlowFile == null){
      log.debug("Invalid ingestion flow file");
      throw new InvalidFileException("Invalid file");
    }
    String filename = StringUtils.defaultString(ingestionFlowFile.getOriginalFilename());
    validateFileExtension(validFileExt, filename);
    validateFilename(filename);
  }

  public static void validateFilename(String filename) {
    if(Stream.of("..", "\\", "/").anyMatch(filename::contains)){
      log.debug("Invalid ingestion flow filename");
      throw new InvalidFileException("Invalid filename");
    }
  }

  private static void validateFileExtension(String validFileExt, String filename) {
    if(!filename.endsWith(validFileExt)){
      log.debug("Invalid ingestion flow file extension");
      throw new InvalidFileException("Invalid file extension");
    }
  }
}
