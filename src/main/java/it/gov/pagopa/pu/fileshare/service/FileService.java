package it.gov.pagopa.pu.fileshare.service;

import it.gov.pagopa.pu.fileshare.exception.custom.InvalidFileException;
import it.gov.pagopa.pu.processexecutions.dto.generated.IngestionFlowFileRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
public class FileService {

  public void validateFile(MultipartFile multipartFile) {
    if (multipartFile == null) {
      log.debug("Invalid ingestion flow file");
      throw new InvalidFileException("INVALID_FILE", "Invalid file");
    }
    String filename = StringUtils.defaultString(multipartFile.getOriginalFilename());
    validateFilename(filename);
  }

  public static void validateFilename(String filename) {
    if (Stream.of("..", "\\", "/").anyMatch(filename::contains)) {
      log.debug("Invalid ingestion flow filename");
      throw new InvalidFileException("INVALID_FILE_NAME", "Invalid filename");
    }
  }

  public String validateVersionFromIngestionFlowFilename(List<String> fileVersions, String fileName, IngestionFlowFileRequestDTO.IngestionFlowFileTypeEnum typeEnum) {
    // This english version exists only for test purpose
    String engVersion = "2_0-eng";
    if (fileName.contains(engVersion) && typeEnum.equals(IngestionFlowFileRequestDTO.IngestionFlowFileTypeEnum.DP_INSTALLMENTS)) {
      return replaceCharVersion(engVersion, "_", ".");
    }
    return fileVersions.stream()
      .filter(fileVersion -> {
        String version = replaceCharVersion(fileVersion, ".", "_");
        return fileName.matches("^.*([^0-9_]|[^0-9]_)" + version + "\\.[^.]+$");
      })
      .findFirst()
      .orElseThrow(() -> new InvalidFileException("INVALID_FILE_NAME", String.format("File name must contain a valid version: %s",
        fileVersions.stream().map(version -> replaceCharVersion(version,".", "_")).toList())));

  }

  private String replaceCharVersion(String fileVersion, String target, String replacement) {
    return fileVersion.replace(target, replacement);
  }
}
