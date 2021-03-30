package com.maviance.middleware_enkap_expresswifi.service.implementations;

import com.maviance.middleware_enkap_expresswifi.enums.ExpressWifiStatus;
import com.maviance.middleware_enkap_expresswifi.enums.RequestSource;
import com.maviance.middleware_enkap_expresswifi.exceptions.ExpressWifiException;
import com.maviance.middleware_enkap_expresswifi.model.request.ExpressWifiPaymentRequest;
import com.maviance.middleware_enkap_expresswifi.model.request.ExpressWifiStatusRequest;
import com.maviance.middleware_enkap_expresswifi.model.request.MiddleWareRequestEntity;
import com.maviance.middleware_enkap_expresswifi.model.response.ENkapOrderResponse;
import com.maviance.middleware_enkap_expresswifi.model.response.RedirectResponse;
import com.maviance.middleware_enkap_expresswifi.model.response.StatusResponse;
import com.maviance.middleware_enkap_expresswifi.repositories.RequestRepository;
import com.maviance.middleware_enkap_expresswifi.service.interfaces.PaymentService;
import com.maviance.middleware_enkap_expresswifi.utils.JsonMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

//@ExtendWith(SpringExtension.class)
@TestPropertySource(locations = {"classpath:application-test.properties"})
@RestClientTest(PaymentServiceImpl.class)
@AutoConfigureWebClient(registerRestTemplate = true)//this is because I have Restemplate as an autowired bean from a Config Class
@ActiveProfiles("dev")
class PaymentServiceTest {

    @Value("${enkap.order.url}")
    String enkapOrderUrl;

    @Value("${enkap.setup.url}")
    String enkapSetupUrl;

    @Value("${enkap.status.url}")
    private String eNkapStatusUrl;

    @MockBean
    RequestRepository requestRepository;

    @Autowired
    PaymentService paymentService;

    @Autowired
    MockRestServiceServer mockRestServiceServer;

    ExpressWifiPaymentRequest paymentRequest = new ExpressWifiPaymentRequest()
            .withAmount(1000.0).withCurrency("XAF").withTimestamp(123454684).withSource(RequestSource.CUSTOMER)
            .withPhoneNumber("6982238440").withPaymentId("payment1")
            .withHmac("f9d64e122dad630f15afeb8da7ace194950423b741153e4397b1ca94f7705518")
            .withCallbackUrl("https://www.florian.com");

    ExpressWifiStatusRequest statusRequest = new ExpressWifiStatusRequest("payment1", "f9d64e122dad630f15afeb8da7ace194950423b741153e4397b1ca94f7705518",
            "PART0001", 123454684);

    @BeforeEach
    void setUp() {
        mockRestServiceServer.reset();
    }

    @AfterEach
    void tearDown() {
        mockRestServiceServer.verify();
    }

    @Test
    @DisplayName("Successful Enkap Payment Request")
    void requestENkapPaymentSuccessFul() {
        ENkapOrderResponse eNkapOrderResponse = new ENkapOrderResponse("PTN1", "payment1", "https://www.enkap.com");

        //mock set up url request
        mockRestServiceServer.expect(ExpectedCount.once(), request -> {
            assertEquals(enkapSetupUrl, request.getURI().toString());
            assertEquals(HttpMethod.PUT, request.getMethod());
        }).andRespond(withSuccess());

        //mock enkap order request
        mockRestServiceServer.expect(ExpectedCount.once(), request -> {
            assertEquals(enkapOrderUrl, request.getURI().toString());
            assertEquals(HttpMethod.POST, request.getMethod());
        }).andRespond(withStatus(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(JsonMapper.objectToJson(eNkapOrderResponse, eNkapOrderResponse.getClass())));


        RedirectResponse redirectResponse = paymentService.requestENkapPayment(paymentRequest);

        assertEquals(ExpressWifiStatus.SUCCESS, redirectResponse.getExpressWifiStatus());
        assertEquals("https://www.enkap.com", redirectResponse.getRedirectUrl());
    }

    @Test
    @DisplayName("Test Enkap Payment Request Payment Id Not Found")
    void requestENkapPaymentPaymentIdNotFound() {

        //mock set up url request
        mockRestServiceServer.expect(ExpectedCount.once(), request -> {
            assertEquals(enkapSetupUrl, request.getURI().toString());
            assertEquals(HttpMethod.PUT, request.getMethod());
        }).andRespond(withSuccess());

        //mock enkap order request
        mockRestServiceServer.expect(ExpectedCount.once(), request -> {
            assertEquals(enkapOrderUrl, request.getURI().toString());
            assertEquals(HttpMethod.POST, request.getMethod());
        }).andRespond(withStatus(HttpStatus.NOT_FOUND));

        ExpressWifiException exception = assertThrows(ExpressWifiException.class, () -> {
            paymentService.requestENkapPayment(paymentRequest);
        });
        assertEquals(ExpressWifiStatus.FAILURE, exception.getErrorResponse().getExpressWifiStatus());
        assertEquals(105, exception.getErrorResponse().getError().getCode());

    }

    @Test
    @DisplayName("Test Enkap Payment Request Payment Id Unauthorized")
    void requestENkapPaymentPaymentIdUnauthorized() {
        //mock set up url request
        mockRestServiceServer.expect(ExpectedCount.once(), request -> {
            assertEquals(enkapSetupUrl, request.getURI().toString());
            assertEquals(HttpMethod.PUT, request.getMethod());
        }).andRespond(withSuccess());

        //mock enkap order request
        mockRestServiceServer.expect(ExpectedCount.once(), request -> {
            assertEquals(enkapOrderUrl, request.getURI().toString());
            assertEquals(HttpMethod.POST, request.getMethod());
        }).andRespond(withUnauthorizedRequest());

        ExpressWifiException exception = assertThrows(ExpressWifiException.class, () -> {
            paymentService.requestENkapPayment(paymentRequest);
        });
        assertEquals(ExpressWifiStatus.FAILURE, exception.getErrorResponse().getExpressWifiStatus());
        assertEquals(500, exception.getErrorResponse().getError().getCode());
    }

    @Test
    void testEnkapPaymentUnexpectedClientError() {
        //mock set up url request
        mockRestServiceServer.expect(ExpectedCount.once(), request -> {
            assertEquals(enkapSetupUrl, request.getURI().toString());
            assertEquals(HttpMethod.PUT, request.getMethod());
        }).andRespond(withSuccess());

        //mock enkap order request
        mockRestServiceServer.expect(ExpectedCount.once(), request -> {
            assertEquals(enkapOrderUrl, request.getURI().toString());
            assertEquals(HttpMethod.POST, request.getMethod());
        }).andRespond(withStatus(HttpStatus.FORBIDDEN));

        ExpressWifiException exception = assertThrows(ExpressWifiException.class, () -> {
            paymentService.requestENkapPayment(paymentRequest);
        });
        assertEquals(ExpressWifiStatus.FAILURE, exception.getErrorResponse().getExpressWifiStatus());
        assertEquals(500, exception.getErrorResponse().getError().getCode());
    }

    @Test
    @DisplayName("Test Invalid Hmac")
    void invalidHmacTest() {
        paymentRequest.setHmac("Invalid Hmac");
        ExpressWifiException exception = assertThrows(ExpressWifiException.class, () -> {
            paymentService.requestENkapPayment(paymentRequest);
        });
        assertEquals(ExpressWifiStatus.FAILURE, exception.getErrorResponse().getExpressWifiStatus());
        assertEquals(102, exception.getErrorResponse().getError().getCode());
    }

    @Test
    @DisplayName("Test Successful ENkap Status Request")
    void requestStatusFromEnkapSucessful() {
        String enkapStatusResponse = "{\"status\": \"CONFIRMED\"}";

        //mock enkap status request
        mockRestServiceServer.expect(MockRestRequestMatchers.requestTo(eNkapStatusUrl.concat("?orderMerchantId=").concat(paymentRequest.getPaymentId())))
                .andRespond(withSuccess(enkapStatusResponse, MediaType.APPLICATION_JSON));
        Mockito.when(requestRepository.findById(ArgumentMatchers.anyString())).thenReturn(Optional.of(new MiddleWareRequestEntity()));
        StatusResponse statusResponse = paymentService.requestStatusFromENkap(statusRequest);

        assertEquals(ExpressWifiStatus.SUCCESS, statusResponse.getStatus());
        Mockito.verify(requestRepository).save(ArgumentMatchers.any(MiddleWareRequestEntity.class));
    }

    @Test
    @DisplayName("Test Status Request Unauthorized")
    void requestStatusFromEnkapUnauthorized() {
        //mock enkap status request
        mockRestServiceServer.expect(MockRestRequestMatchers.requestTo(eNkapStatusUrl.concat("?orderMerchantId=").concat(paymentRequest.getPaymentId())))
                .andRespond(withUnauthorizedRequest());
        ExpressWifiException expressWifiException = assertThrows(ExpressWifiException.class, () -> {
            paymentService.requestStatusFromENkap(statusRequest);
        });

        assertEquals(500, expressWifiException.getErrorResponse().getError().getCode());
        assertEquals(ExpressWifiStatus.FAILURE, expressWifiException.getErrorResponse().getExpressWifiStatus());
    }

    @Test
    @DisplayName("Test Status Request Not Found")
    void requestStatusFromEnkapNotFound() {
        //mock enkap status request
        mockRestServiceServer.expect(MockRestRequestMatchers.requestTo(eNkapStatusUrl.concat("?orderMerchantId=").concat(paymentRequest.getPaymentId())))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));
        ExpressWifiException expressWifiException = assertThrows(ExpressWifiException.class, () -> {
            paymentService.requestStatusFromENkap(statusRequest);
        });

        assertEquals(105, expressWifiException.getErrorResponse().getError().getCode());
        assertEquals(ExpressWifiStatus.FAILURE, expressWifiException.getErrorResponse().getExpressWifiStatus());
    }


}