package it.gov.pagopa.pu.fileshare.controller;

import it.gov.pagopa.pu.fileshare.controller.generated.ReceiptApi;
import it.gov.pagopa.pu.fileshare.security.SecurityUtils;
import it.gov.pagopa.pu.fileshare.service.receipt.ReceiptRtRetrieverService;
import it.gov.pagopa.pu.fileshare.util.Utilities;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class ReceiptController implements ReceiptApi {

  private final ReceiptRtRetrieverService receiptRtRetrieverService;

  public ReceiptController(ReceiptRtRetrieverService receiptRtRetrieverService) {
    this.receiptRtRetrieverService = receiptRtRetrieverService;
  }

  @Override
  public ResponseEntity<Resource> downloadRt(Long organizationId, Long receiptId) {
    log.info("Retrieving RT file of receiptId {} related to organization {}", receiptId, organizationId);
    return Utilities.buildResourceResponseEntity(
      receiptRtRetrieverService.downloadRt(
        organizationId,
        receiptId,
        SecurityUtils.getLoggedUser(),
        SecurityUtils.getAccessToken())
    );
  }
}
