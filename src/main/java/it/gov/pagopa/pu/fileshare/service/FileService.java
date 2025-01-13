package it.gov.pagopa.pu.fileshare.service;

import it.gov.pagopa.pu.fileshare.exception.custom.FileUploadException;
import it.gov.pagopa.pu.fileshare.exception.custom.InvalidFileException;
import it.gov.pagopa.pu.fileshare.util.AESUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class FileService {
  private final String sharedFolderRootPath;
  private final String fileEncryptPassword;

  public FileService(@Value("${folders.shared}") String sharedFolderRootPath,
    @Value(("${app.fileEncryptPassword}")) String fileEncryptPassword) {
    this.sharedFolderRootPath = sharedFolderRootPath;
    this.fileEncryptPassword = fileEncryptPassword;
  }

  public void validateFile(MultipartFile ingestionFlowFile, String validFileExt) {
    if( ingestionFlowFile == null){
      log.debug("Invalid ingestion flow file");
      throw new InvalidFileException("Invalid file");
    }
    String filename = StringUtils.defaultString(ingestionFlowFile.getOriginalFilename());
    validateFileExtension(validFileExt, filename);
    validateFilename(filename);
  }

  private static void validateFilename(String filename) {
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

  public void saveToSharedFolder(MultipartFile file, String relativePath){
    if(file==null){
      log.debug("File is mandatory");
      throw new FileUploadException("File is mandatory");
    }

    String filename = org.springframework.util.StringUtils.cleanPath(StringUtils.defaultString(file.getOriginalFilename()));
    validateFilename(filename);
    Path fileLocation = Paths.get(sharedFolderRootPath, relativePath, filename);
    //create missing parent folder, if any
    try {
      if (!fileLocation.toAbsolutePath().getParent().toFile().exists())
        Files.createDirectories(fileLocation.toAbsolutePath().getParent());
      encryptAndSaveFile(file, fileLocation);
    }catch (Exception e) {
      log.debug(
        "Error uploading file to folder %s%s".formatted(sharedFolderRootPath,
          relativePath), e);
      throw new FileUploadException(
        "Error uploading file to folder %s%s [%s]".formatted(
          sharedFolderRootPath, relativePath, e.getMessage()));
    }
    log.debug("File upload to folder %s%s completed".formatted(sharedFolderRootPath,
      relativePath));
  }

  private void encryptAndSaveFile(MultipartFile file, Path fileLocation)
    throws IOException {
    try(InputStream is = file.getInputStream();
      InputStream cipherIs = AESUtils.encrypt(fileEncryptPassword, is)){
      Files.copy(cipherIs, fileLocation, StandardCopyOption.REPLACE_EXISTING);
    }
  }
}
