package it.gov.pagopa.pu.fileshare.connector.pagopapayments;

import it.gov.pagopa.pu.fileshare.connector.pagopapayments.client.PrintPaymentNoticeClient;
import it.gov.pagopa.pu.pagopapayments.dto.generated.SignedUrlResultDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Lazy
@Service
@Slf4j
public class PrintPaymentNoticeServiceImpl implements PrintPaymentNoticeService {
	private final PrintPaymentNoticeClient printPaymentNoticeClient;

	public PrintPaymentNoticeServiceImpl(PrintPaymentNoticeClient printPaymentNoticeClient) {
		this.printPaymentNoticeClient = printPaymentNoticeClient;
	}

	@Override
	public SignedUrlResultDTO getSignedUrl(Long organizationId, String pdfGeneratedId, String accessToken) {
		log.info("Get signed url for organizationId: {} and pdfGeneratedId: {}", organizationId, pdfGeneratedId);
		return printPaymentNoticeClient.getSignedUrl(organizationId, pdfGeneratedId, accessToken);
	}
}
