package it.gov.pagopa.pu.fileshare.exception;

import it.gov.pagopa.pu.fileshare.dto.generated.FileshareErrorDTO;
import it.gov.pagopa.pu.fileshare.dto.generated.FileshareErrorDTO.CodeEnum;
import it.gov.pagopa.pu.fileshare.exception.custom.InvalidFileException;
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
 *
 */
@RestControllerAdvice
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
public class FileshareExceptionHandler {

  @ExceptionHandler({InvalidFileException.class})
  public ResponseEntity<FileshareErrorDTO> handleInvalidFileError(RuntimeException ex, HttpServletRequest request){
    return handleFileshareErrorException(ex, request, HttpStatus.BAD_REQUEST, CodeEnum.INVALID_FILE);
  }

  static ResponseEntity<FileshareErrorDTO> handleFileshareErrorException(RuntimeException ex, HttpServletRequest request, HttpStatus httpStatus, FileshareErrorDTO.CodeEnum errorEnum) {
    String message = logException(ex, request, httpStatus);

    return ResponseEntity
      .status(httpStatus)
      .body(new FileshareErrorDTO(errorEnum, message));
  }

  private static String logException(RuntimeException ex, HttpServletRequest request, HttpStatus httpStatus) {
    String message = ex.getMessage();
    log.info("A {} occurred handling request {}: HttpStatus {} - {}",
      ex.getClass(),
      getRequestDetails(request),
      httpStatus.value(),
      message);
    return message;
  }

  static String getRequestDetails(HttpServletRequest request) {
    return "%s %s".formatted(request.getMethod(), request.getRequestURI());
  }
}
