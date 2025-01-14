package it.gov.pagopa.pu.fileshare.service;

import it.gov.pagopa.pu.fileshare.config.FoldersPathsConfig;
import it.gov.pagopa.pu.fileshare.exception.custom.FileUploadException;
import it.gov.pagopa.pu.fileshare.exception.custom.InvalidFileException;
import it.gov.pagopa.pu.fileshare.util.AESUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class FileStorerService {
  private final FoldersPathsConfig foldersPathsConfig;
  private final String fileEncryptPassword;

  public FileStorerService(FoldersPathsConfig foldersPathsConfig,
    @Value(("${app.fileEncryptPassword}")) String fileEncryptPassword) {
    this.foldersPathsConfig = foldersPathsConfig;
    this.fileEncryptPassword = fileEncryptPassword;
  }

  private Path getFilePath(String relativePath, String filename) {
    String basePath = foldersPathsConfig.getShared()+relativePath;
    Path fileLocation = Paths.get(basePath,filename).normalize();
    if(!fileLocation.startsWith(basePath)){
      log.debug("Invalid file path");
      throw new InvalidFileException("Invalid file path");
    }
    return fileLocation;
  }

  public String saveToSharedFolder(MultipartFile file, String relativePath){
    if(file==null){
      log.debug("File is mandatory");
      throw new FileUploadException("File is mandatory");
    }

    String sharedFolderRootPath = foldersPathsConfig.getShared();
    String filename = org.springframework.util.StringUtils.cleanPath(
      StringUtils.defaultString(file.getOriginalFilename()));
    FileService.validateFilename(filename);
    Path fileLocation = getFilePath(relativePath, filename);
    //create missing parent folder, if any
    try {
      if (!Files.exists(fileLocation.getParent()))
        Files.createDirectories(fileLocation.getParent());
      encryptAndSaveFile(file, fileLocation);
    }catch (Exception e) {
      String errorMessage = "Error uploading file to folder %s%s".formatted(
        sharedFolderRootPath,
        relativePath);
      log.debug(
        errorMessage, e);
      throw new FileUploadException(
        errorMessage);
    }
    log.debug("File upload to folder %s%s completed".formatted(sharedFolderRootPath,
      relativePath));
    return Paths.get(relativePath,filename).toString();
  }

  private void encryptAndSaveFile(MultipartFile file, Path fileLocation)
    throws IOException {
    try(InputStream is = file.getInputStream();
      InputStream cipherIs = AESUtils.encrypt(fileEncryptPassword, is)){
      Files.copy(cipherIs, fileLocation, StandardCopyOption.REPLACE_EXISTING);
    }
  }
}
