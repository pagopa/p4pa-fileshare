package it.gov.pagopa.pu.fileshare.connector.debtpositions;

import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptNoPII;

public interface ReceiptService {
  ReceiptNoPII getReceiptById(Long receiptId, String accessToken);
}
