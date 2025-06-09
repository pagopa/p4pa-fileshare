package it.gov.pagopa.pu.fileshare.connector.pagopapayments.client;


import it.gov.pagopa.pu.fileshare.connector.pagopapayments.config.PagoPaPaymentsApisHolder;
import it.gov.pagopa.pu.pagopapayments.dto.generated.SignedUrlResultDTO;
import org.springframework.stereotype.Service;


@Service
public class PrintPaymentNoticeClient {
	private final PagoPaPaymentsApisHolder pagoPaPaymentsApisHolder;

	public PrintPaymentNoticeClient(PagoPaPaymentsApisHolder pagoPaPaymentsApisHolder) {
		this.pagoPaPaymentsApisHolder = pagoPaPaymentsApisHolder;
	}

	public SignedUrlResultDTO getSignedUrl(Long organizationId, String pdfGeneratedId, String accessToken) {
		return pagoPaPaymentsApisHolder.getPrintPaymentNoticeApi(accessToken).getSignedUrl(organizationId, pdfGeneratedId);
	}
}
