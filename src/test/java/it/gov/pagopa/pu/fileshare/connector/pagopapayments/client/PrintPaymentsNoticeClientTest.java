package it.gov.pagopa.pu.fileshare.connector.pagopapayments.client;


import it.gov.pagopa.pu.fileshare.connector.pagopapayments.config.PagoPaPaymentsApisHolder;
import it.gov.pagopa.pu.pagopapayments.client.generated.PrintPaymentNoticeApi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PrintPaymentsNoticeClientTest {

  @Mock
  private PagoPaPaymentsApisHolder pagoPaPaymentsApisHolderMock;
  @Mock
  private PrintPaymentNoticeApi printPaymentNoticeApiMock;

  private PrintPaymentNoticeClient printPaymentNoticeClient;


  @BeforeEach
  void setUp() {
    printPaymentNoticeClient = new PrintPaymentNoticeClient(pagoPaPaymentsApisHolderMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      pagoPaPaymentsApisHolderMock
    );
  }

  @Test
  void whenGetSignedUrlThenInvokeWithAccessToken() {
    // Given
    String accessToken = "ACCESSTOKEN";
    Long orgId = 1L;
    String pdfGeneratedId = "pdfGeneratedId";

    Mockito.when(pagoPaPaymentsApisHolderMock.getPrintPaymentNoticeApi(accessToken))
      .thenReturn(printPaymentNoticeApiMock);

    // When
    printPaymentNoticeClient.getSignedUrl(orgId, pdfGeneratedId, accessToken);

    // Then
    Mockito.verify(printPaymentNoticeApiMock)
      .getSignedUrl(orgId, pdfGeneratedId);
  }
}
