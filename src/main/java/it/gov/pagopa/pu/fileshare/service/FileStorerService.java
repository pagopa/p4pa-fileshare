package it.gov.pagopa.pu.fileshare.service;

import it.gov.pagopa.pu.fileshare.config.FoldersPathsConfig;
import it.gov.pagopa.pu.fileshare.dto.SaveFileResultDTO;
import it.gov.pagopa.pu.fileshare.exception.custom.FileUploadException;
import it.gov.pagopa.pu.fileshare.exception.custom.InvalidFileException;
import it.gov.pagopa.pu.fileshare.util.AESUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
public class FileStorerService {
  private final FoldersPathsConfig foldersPathsConfig;
  private final String fileEncryptPassword;

  public FileStorerService(FoldersPathsConfig foldersPathsConfig,
                           @Value(("${app.fileEncryptPassword}")) String fileEncryptPassword) {
    if (foldersPathsConfig.getShared() == null || foldersPathsConfig.getShared().isEmpty()) {
      throw new IllegalStateException("Shared folder path is not configured.");
    }
    this.foldersPathsConfig = foldersPathsConfig;
    this.fileEncryptPassword = fileEncryptPassword;
  }

  public SaveFileResultDTO saveToSharedFolder(Long organizationId, MultipartFile file, String relativePath, String fileName) {
    if (file == null) {
      log.debug("File is mandatory");
      throw new FileUploadException("File is mandatory");
    }

    fileName = org.springframework.util.StringUtils.cleanPath(StringUtils.defaultString(fileName));
    FileService.validateFilename(fileName);
    byte[] fileHash;
    Path relativeFileLocation = concatenatePaths(relativePath, fileName);
    Path organizationBasePath = buildOrganizationBasePath(organizationId);
    Path absolutePath = concatenatePaths(organizationBasePath.toString(), relativeFileLocation.toString());
    //create missing parent folder, if any
    try {
      if (!Files.exists(absolutePath.getParent())) {
        Files.createDirectories(absolutePath.getParent());
      }
      fileHash = AESUtils.encryptAndSave(fileEncryptPassword, file.getInputStream(), absolutePath.getParent(), absolutePath.getFileName().toString());
    } catch (Exception e) {
      throw new FileUploadException(
        "Error uploading file to shared folder %s".formatted(relativePath), e);
    }
    log.debug("File upload to shared folder {} completed", relativePath);
    return new SaveFileResultDTO(relativePath, fileHash);
  }

  /**
   * This method expects two paths whose concatenation does not resolve into an outer folder.
   * The normalized path still starts with the first path.
   */
  public static Path concatenatePaths(String firstPath, String secondPath) {
    Path concatenatedPath = Paths.get(firstPath, secondPath).normalize();
    if (!concatenatedPath.startsWith(firstPath)) {
      log.debug("Invalid file path");
      throw new InvalidFileException("Invalid file path");
    }
    return concatenatedPath;
  }

  public InputStream decryptFile(Path filePath, String fileName) {
    return AESUtils.decrypt(fileEncryptPassword, filePath, fileName);
  }

  public Path buildOrganizationBasePath(Long organizationId) {
    return concatenatePaths(foldersPathsConfig.getShared(), String.valueOf(organizationId));
  }

  public boolean checkIfAlreadyUploadedOrArchived(Long organizationId, String archivedSubFolder, String ingestionFlowFilePath, String fileName) {
    return getUploadedOrArchivedPath(organizationId, archivedSubFolder, ingestionFlowFilePath, fileName) != null;
  }

  public Path getUploadedOrArchivedPath(Long organizationId, String archivedSubFolder, String ingestionFlowFilePath, String fileName) {
    Path filePath = buildOrganizationBasePath(organizationId)
      .resolve(ingestionFlowFilePath);
    String fileNameCiphered = fileName + AESUtils.CIPHER_EXTENSION;
    Path originalPath = FileStorerService.concatenatePaths(filePath.toString(), fileNameCiphered);
    if (Files.exists(originalPath)) {
      return originalPath;
    } else {
      Path archivedPath = FileStorerService.concatenatePaths(filePath.resolve(archivedSubFolder).toString(), fileNameCiphered);
      if (Files.exists(archivedPath)) {
        return archivedPath;
      } else {
        return null;
      }
    }
  }

}
