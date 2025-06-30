package it.gov.pagopa.pu.fileshare.connector.debtpositions.client;

import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptNoPII;
import it.gov.pagopa.pu.fileshare.connector.debtpositions.config.DebtPositionsApisHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Slf4j
@Service
public class ReceiptClient {
  private final DebtPositionsApisHolder debtPositionsApisHolder;

  public ReceiptClient(DebtPositionsApisHolder debtPositionsApisHolder) {
    this.debtPositionsApisHolder = debtPositionsApisHolder;
  }

  public ReceiptNoPII getReceiptById(Long receiptId, String accessToken) {
    try {
      return debtPositionsApisHolder.getReceiptNoPiiEntityControllerApi(accessToken)
        .crudGetReceiptnopii(String.valueOf(receiptId));
    } catch (HttpClientErrorException.NotFound e) {
      log.info("Receipt with id {} not found", receiptId);
      return null;
    }
  }
}
