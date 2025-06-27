package it.gov.pagopa.pu.fileshare.connector.debtpositions;

import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptNoPII;
import it.gov.pagopa.pu.fileshare.connector.debtpositions.client.ReceiptClient;
import org.springframework.stereotype.Service;

@Service
public class ReceiptServiceImpl implements ReceiptService {

  private final ReceiptClient client;

  public ReceiptServiceImpl(ReceiptClient client) {
    this.client = client;
  }

  @Override
  public ReceiptNoPII getReceiptById(Long receiptId, String accessToken) {
    return client.getReceiptById(receiptId, accessToken);
  }
}
