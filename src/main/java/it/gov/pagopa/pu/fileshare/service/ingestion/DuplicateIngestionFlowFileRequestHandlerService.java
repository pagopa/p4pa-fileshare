package it.gov.pagopa.pu.fileshare.service.ingestion;

import it.gov.pagopa.pu.fileshare.connector.processexecutions.IngestionFlowFileService;
import it.gov.pagopa.pu.fileshare.exception.custom.FileAlreadyExistsException;
import it.gov.pagopa.pu.fileshare.service.FileStorerService;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFile;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFileStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

@Service
@Slf4j
public class DuplicateIngestionFlowFileRequestHandlerService {

  private static final Set<IngestionFlowFileStatus> RETRYABLE_STATUSES = Set.of(
    IngestionFlowFileStatus.ERROR,
    IngestionFlowFileStatus.WARNING
  );

  private final IngestionFlowFileService ingestionFlowFileService;
  private final FileStorerService fileStorerService;

  public DuplicateIngestionFlowFileRequestHandlerService(IngestionFlowFileService ingestionFlowFileService, FileStorerService fileStorerService) {
    this.ingestionFlowFileService = ingestionFlowFileService;
    this.fileStorerService = fileStorerService;
  }

  void handleDuplicateFile(Long organizationId, String archivedSubFolder, String filePathName, String fileName, String accessToken) {
    IngestionFlowFile ingestionFlowFile = ingestionFlowFileService.findByOrganizationIdAndFilePathNameAndFileName(organizationId, filePathName, fileName, accessToken);
    String newFileName;
    if (ingestionFlowFile != null) {
      if (RETRYABLE_STATUSES.contains(ingestionFlowFile.getStatus())) {
        String fileNameSuffix = "_" + ingestionFlowFile.getStatus() + "_" + ingestionFlowFile.getIngestionFlowFileId();
        newFileName = archiveNotCorrectedHandledFile(organizationId, archivedSubFolder, filePathName, fileName, fileNameSuffix);

        String newDiscardFileName = ingestionFlowFile.getDiscardFileName();
        if (StringUtils.isNotEmpty(newDiscardFileName)) {
          newDiscardFileName = archiveNotCorrectedHandledFile(organizationId, archivedSubFolder, filePathName, newDiscardFileName, fileNameSuffix);
        }

        ingestionFlowFileService.updateFileNames(ingestionFlowFile.getIngestionFlowFileId(), newFileName, newDiscardFileName, accessToken);
      } else {
        throw new FileAlreadyExistsException("FILE_ALREADY_EXISTS", "File already uploaded or archived");
      }
    } else {
      newFileName = archiveNotCorrectedHandledFile(organizationId, archivedSubFolder, filePathName, fileName, "_UNKNOWN_" + System.currentTimeMillis());
    }
    log.info("Uploading an already existent file! Renamed from {} into {}", fileName, newFileName);
  }

  private String archiveNotCorrectedHandledFile(Long organizationId, String archivedSubFolder, String filePathName, String fileName, String fileNameSuffix) {
    Path filePath = fileStorerService.getUploadedOrArchivedPath(organizationId, archivedSubFolder, filePathName, fileName);
    String newFileName = fileName.replaceFirst("(\\..*)$", fileNameSuffix + "$1"); // preserve original extension

    if (filePath != null) {
      Path archiveFolderPath = getArchiveFolderPath(archivedSubFolder, filePath);
      Path renamedPath = archiveFolderPath.resolve(filePath.getFileName().toString().replace(fileName, newFileName));
      renameFile(filePath, renamedPath);
    } else {
      log.info("Cannot rename file! It doesn't exists! organizationId {}, filePathName {}, fileName {}",
        organizationId, filePathName, fileName);
    }
    return newFileName;
  }

  private Path getArchiveFolderPath(String archivedSubFolder, Path filePath) {
    Path parentFolder = filePath.getParent();
    if(!parentFolder.getFileName().toString().equals(archivedSubFolder)){
      parentFolder = parentFolder.resolve(archivedSubFolder);
      try {
        Files.createDirectories(parentFolder);
      } catch (IOException e) {
        throw new IllegalStateException("[FOLDER_CREATION_ERROR] Cannot create archive subfolder " + parentFolder + ": " + e.getMessage(), e);
      }
    }
    return parentFolder;
  }

  private static void renameFile(Path filePath, Path renamedPath) {
    try {
      Files.move(filePath, renamedPath);
      log.info("Renamed file {} into {}", filePath, renamedPath);
    } catch (IOException e) {
      throw new IllegalStateException("[FILE_RENAMING_ERROR] Cannot rename file " + filePath + " into " + renamedPath + ": " + e.getMessage(), e);
    }
  }
}
