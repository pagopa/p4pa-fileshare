package it.gov.pagopa.pu.fileshare.service.receipt;

import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptNoPII;
import it.gov.pagopa.pu.fileshare.connector.debtpositions.ReceiptService;
import it.gov.pagopa.pu.fileshare.dto.FileResourceDTO;
import it.gov.pagopa.pu.fileshare.exception.custom.FileNotFoundException;
import it.gov.pagopa.pu.fileshare.exception.custom.OrganizationMissMatchException;
import it.gov.pagopa.pu.fileshare.exception.custom.ReceiptNotFoundException;
import it.gov.pagopa.pu.fileshare.service.AuthorizationService;
import it.gov.pagopa.pu.fileshare.service.FileStorerService;
import it.gov.pagopa.pu.p4paauth.dto.generated.UserInfo;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.file.Path;

@Service
public class ReceiptRtRetrieverServiceImpl implements ReceiptRtRetrieverService {

  private final ReceiptService receiptService;
  private final FileStorerService fileStorerService;

  public ReceiptRtRetrieverServiceImpl(
    ReceiptService receiptService,
    FileStorerService fileStorerService) {
    this.receiptService = receiptService;
    this.fileStorerService = fileStorerService;
  }

  @Override
  public FileResourceDTO downloadRt(Long organizationId, Long receiptId, UserInfo user, String accessToken) {
    AuthorizationService.validateAdminRole(organizationId, user);

    ReceiptNoPII receipt = receiptService.getReceiptById(receiptId, accessToken);
    if(receipt == null){
      throw new ReceiptNotFoundException("RECEIPT_NOT_FOUND", "Cannot find receipt having id " + receiptId);
    }
    if(!receipt.getOrgFiscalCode().equals(AuthorizationService.getOrgFiscalCodeFromUserInfo(user, organizationId))){
      throw new OrganizationMissMatchException("INVALID_RECEIPT_ORG_MISMATCH", "Requested receipt ("+ receiptId + ") is not related to the provided organization ("+organizationId+")");
    }

    if(receipt.getRtFilePath()!=null) {
      Path rtFullPath = fileStorerService.buildOrganizationBasePath(organizationId)
        .resolve(receipt.getRtFilePath());

      String fileName = rtFullPath.getFileName().toString();
      InputStream decryptedInputStream = fileStorerService.decryptFile(rtFullPath.getParent(), fileName);

      return new FileResourceDTO(new InputStreamResource(decryptedInputStream), fileName);
    } else {
      throw new FileNotFoundException("RECEIPT_NOT_FOUND", "RT related to receipt "+ receiptId + " not available");
    }
  }
}
