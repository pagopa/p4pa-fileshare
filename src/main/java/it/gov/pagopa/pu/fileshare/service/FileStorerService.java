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

  public String saveToSharedFolder(MultipartFile file, String relativePath){
    if(file==null){
      log.debug("File is mandatory");
      throw new FileUploadException("File is mandatory");
    }

    String filename = org.springframework.util.StringUtils.cleanPath(
      StringUtils.defaultString(file.getOriginalFilename()));
    FileService.validateFilename(filename);
    Path fileLocation = concatenatePaths(relativePath,filename);
    Path absolutePath = concatenatePaths(foldersPathsConfig.getShared(), fileLocation.toString());
    //create missing parent folder, if any
    try {
      if (!Files.exists(absolutePath.getParent())){
        Files.createDirectories(absolutePath.getParent());
      }
      encryptAndSaveFile(file, absolutePath);
    }catch (Exception e) {
      String errorMessage = "Error uploading file to shared folder %s".formatted(
        relativePath);
      log.debug(
        errorMessage, e);
      throw new FileUploadException(
        errorMessage);
    }
    log.debug("File upload to shared folder {} completed",relativePath);
    return fileLocation.toString();
  }

  private Path concatenatePaths(String firstPath, String secondPath) {
    Path concatenatedPath = Paths.get(firstPath, secondPath).normalize();
    if(!concatenatedPath.startsWith(firstPath)){
      log.debug("Invalid file path");
      throw new InvalidFileException("Invalid file path");
    }
    return concatenatedPath;
  }

  private void encryptAndSaveFile(MultipartFile file, Path fileLocation)
    throws IOException {
    try(InputStream is = file.getInputStream();
      InputStream cipherIs = AESUtils.encrypt(fileEncryptPassword, is)){
      Files.copy(cipherIs, fileLocation, StandardCopyOption.REPLACE_EXISTING);
    }
  }
}
