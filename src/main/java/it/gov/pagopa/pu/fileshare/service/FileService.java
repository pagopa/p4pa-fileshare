package it.gov.pagopa.pu.fileshare.service;

import it.gov.pagopa.pu.fileshare.exception.custom.InvalidFileException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
public class FileService {

  public void validateFile(MultipartFile ingestionFlowFile) {
    if (ingestionFlowFile == null) {
      log.debug("Invalid ingestion flow file");
      throw new InvalidFileException("Invalid file");
    }
    String filename = StringUtils.defaultString(ingestionFlowFile.getOriginalFilename());
    validateFilename(filename);
  }

  public static void validateFilename(String filename) {
    if (Stream.of("..", "\\", "/").anyMatch(filename::contains)) {
      log.debug("Invalid ingestion flow filename");
      throw new InvalidFileException("Invalid filename");
    }
  }

  public String validateVersionFromIngestionFlowFilename(List<String> fileVersions, String fileName) {
    // This english version exists only for test purpose
    String engVersion = "2_0-eng";
    if (fileName.contains(engVersion)) {
      return replaceVersion(engVersion, "_", ".");
    }
    return fileVersions.stream()
      .filter(fileVersion -> {
        String version = replaceVersion(fileVersion, ".", "_");
        return fileName.contains(version);
      })
      .findFirst()
      .orElseThrow(() -> new InvalidFileException(String.format("File name must contain a valid version: %s",
        fileVersions.stream().map(v -> v.replace(".", "_")).toList())));

  }

  private String replaceVersion(String fileVersion, String target, String replacement) {
    return fileVersion.replace(target, replacement);
  }
}
