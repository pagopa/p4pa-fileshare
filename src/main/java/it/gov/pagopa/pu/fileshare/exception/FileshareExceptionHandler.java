package it.gov.pagopa.pu.fileshare.exception;

import it.gov.pagopa.pu.fileshare.dto.generated.FileshareErrorDTO;
import it.gov.pagopa.pu.fileshare.dto.generated.FileshareErrorDTO.CategoryEnum;
import it.gov.pagopa.pu.fileshare.exception.common.CommonExceptionHandler;
import it.gov.pagopa.pu.fileshare.exception.custom.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * A class exception that handles errors related to workflows.
 */
@RestControllerAdvice
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
public class FileshareExceptionHandler extends CommonExceptionHandler {

  @ExceptionHandler({IngestionFlowFileNotFoundException.class, OrganizationMissMatchException.class, FileNotFoundException.class, ReceiptNotFoundException.class})
  public ResponseEntity<FileshareErrorDTO> handleNotFoundException(RuntimeException ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.NOT_FOUND, CategoryEnum.NOT_FOUND);
  }

  @ExceptionHandler({InvalidFileException.class})
  public ResponseEntity<FileshareErrorDTO> handleInvalidFileError(RuntimeException ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.BAD_REQUEST, CategoryEnum.INVALID_FILE);
  }

  @ExceptionHandler(InvalidFileTypeException.class)
  public ResponseEntity<FileshareErrorDTO> handleInvalidFileTypeException(RuntimeException ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.BAD_REQUEST, CategoryEnum.INVALID_FILE_TYPE);
  }

  @ExceptionHandler({UnauthorizedFileDownloadException.class})
  public ResponseEntity<FileshareErrorDTO> handleUnauthorizedFileDownloadError(RuntimeException ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.UNAUTHORIZED, CategoryEnum.UNAUTHORIZED);
  }

  @ExceptionHandler({FileUploadException.class})
  public ResponseEntity<FileshareErrorDTO> handleFileStorageError(RuntimeException ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.INTERNAL_SERVER_ERROR, CategoryEnum.FILE_UPLOAD_ERROR);
  }

  @ExceptionHandler({java.io.FileNotFoundException.class})
  public ResponseEntity<FileshareErrorDTO> handleFileNotFoundException(
    java.io.FileNotFoundException ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.NOT_FOUND, CategoryEnum.NOT_FOUND);
  }

  @ExceptionHandler({FileAlreadyExistsException.class, java.nio.file.FileAlreadyExistsException.class})
  public ResponseEntity<FileshareErrorDTO> handleFileAlreadyExistsException(Exception ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.CONFLICT, CategoryEnum.CONFLICT);
  }

}
