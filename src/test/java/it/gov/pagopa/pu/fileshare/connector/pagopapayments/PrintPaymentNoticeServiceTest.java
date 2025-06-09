package it.gov.pagopa.pu.fileshare.connector.pagopapayments;


import it.gov.pagopa.pu.fileshare.connector.pagopapayments.client.PrintPaymentNoticeClient;
import it.gov.pagopa.pu.pagopapayments.dto.generated.SignedUrlResultDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrintPaymentNoticeServiceTest {
    @Mock
    private PrintPaymentNoticeClient printPaymentNoticeClientMock;

    private PrintPaymentNoticeService service;


    @BeforeEach
    void setUp() {
        service = new PrintPaymentNoticeServiceImpl(printPaymentNoticeClientMock);
    }

    @AfterEach
    void tearDown() {
        Mockito.verifyNoMoreInteractions(printPaymentNoticeClientMock);
    }

    @Test
    void whenGetSignedUrlThenInvokeClient() {
        // Given
        String accessToken = "accessToken";
        Long orgId = 1L;
        String pdfGeneratedId = "pdfGeneratedId";
        SignedUrlResultDTO response = new SignedUrlResultDTO(null, null, "url");

        when(printPaymentNoticeClientMock.getSignedUrl(orgId, pdfGeneratedId, accessToken))
          .thenReturn(response);

        // When
        SignedUrlResultDTO result = service.getSignedUrl(orgId, pdfGeneratedId, accessToken);

        // Then
        assertEquals(response, result);
    }
}
