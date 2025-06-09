package it.gov.pagopa.pu.fileshare.connector.pagopapayments;

import it.gov.pagopa.pu.pagopapayments.dto.generated.SignedUrlResultDTO;

/**
 * This interface provides a method for print payment notice on PagoPa service
 */
public interface PrintPaymentNoticeService {

	/**
	 * Return SignedUrlResultDTO with signedUrl if generation request status is ready and two lists containing all notices processed or in error
	 *
	 * @param organizationId the ID of the organization
	 * @param pdfGeneratedId retrieved from generateMassive
	 * @return SignedUrlResultDTO
	 */
	SignedUrlResultDTO getSignedUrl(Long organizationId, String pdfGeneratedId, String accessToken);
}
