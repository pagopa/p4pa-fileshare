package it.gov.pagopa.pu.fileshare.service.receipt;

import it.gov.pagopa.pu.fileshare.dto.FileResourceDTO;
import it.gov.pagopa.pu.auth.dto.generated.UserInfo;

public interface ReceiptRtRetrieverService {
  FileResourceDTO downloadRt(Long organizationId, Long receiptId, UserInfo user, String accessToken);
}
