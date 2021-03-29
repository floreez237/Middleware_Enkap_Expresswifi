package com.maviance.middleware_enkap_expresswifi.service.implementations;

import com.maviance.middleware_enkap_expresswifi.enums.ExpessWifiStatus;
import com.maviance.middleware_enkap_expresswifi.enums.RequestSource;
import com.maviance.middleware_enkap_expresswifi.exceptions.ExpressWifiException;
import com.maviance.middleware_enkap_expresswifi.model.request.ENkapOrderRequest;
import com.maviance.middleware_enkap_expresswifi.model.request.ExpressWifiPaymentRequest;
import com.maviance.middleware_enkap_expresswifi.model.request.ExpressWifiStatusRequest;
import com.maviance.middleware_enkap_expresswifi.model.response.ENkapOrderResponse;
import com.maviance.middleware_enkap_expresswifi.model.response.RedirectResponse;
import com.maviance.middleware_enkap_expresswifi.model.response.StatusResponse;
import com.maviance.middleware_enkap_expresswifi.service.interfaces.PaymentService;
import com.maviance.middleware_enkap_expresswifi.utils.JsonMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {PaymentServiceTest.Config.class})
@TestPropertySource(locations = {"classpath:application.properties"})
class PaymentServiceTest {

    @MockBean
    RestTemplate restTemplate;

    @Autowired
    PaymentService paymentService;
    ExpressWifiPaymentRequest paymentRequest = new ExpressWifiPaymentRequest()
            .withAmount(1000.0).withCurrency("XAF").withTimestamp(123454684).withSource(RequestSource.CUSTOMER)
            .withPhoneNumber("6982238440").withPaymentId("payment1")
            .withHmac("f9d64e122dad630f15afeb8da7ace194950423b741153e4397b1ca94f7705518")
            .withCallbackUrl("https://www.florian.com");

    ExpressWifiStatusRequest statusRequest = new ExpressWifiStatusRequest("payment1", "f9d64e122dad630f15afeb8da7ace194950423b741153e4397b1ca94f7705518",
            "PART0001", 123454684);

    @Test
    @DisplayName("Successful Enkap Payment Request")
    void requestENkapPaymentSuccessFul() {
        ENkapOrderResponse eNkapOrderResponse = new ENkapOrderResponse("PTN1", "payment1", "https://www.enkap.com");

        ResponseEntity<String> responseEntity = ResponseEntity.status(HttpStatus.CREATED)
                .body(JsonMapper.objectToJson(eNkapOrderResponse, eNkapOrderResponse.getClass()));
        ArgumentCaptor<ENkapOrderRequest> requestArgumentCaptor = ArgumentCaptor.forClass(ENkapOrderRequest.class);
        //mock enkap order request
        when(restTemplate.postForEntity(any(String.class), requestArgumentCaptor.capture(), any(Class.class)))
                .thenReturn(responseEntity);
        //mock set up url request
        Mockito.when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), any(Class.class)))
                .thenReturn(ResponseEntity.ok().build());
        RedirectResponse redirectResponse = paymentService.requestENkapPayment(paymentRequest);

        System.out.println("\n\n".concat(requestArgumentCaptor.getValue().toString()).concat("\n"));
        assertEquals(ExpessWifiStatus.SUCCESS, redirectResponse.getExpessWifiStatus());
        assertEquals("https://www.enkap.com", redirectResponse.getRedirectUrl());
    }

    @Test
    @DisplayName("Test Enkap Payment Request Payment Id Not Found")
    void requestENkapPaymentPaymentIdNotFound() {

        ResponseEntity<String> responseEntity = ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        //mock enkap order request
        when(restTemplate.postForEntity(any(String.class), any(ENkapOrderRequest.class), any(Class.class)))
                .thenReturn(responseEntity);
        //mock set up url request
        Mockito.when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), any(Class.class)))
                .thenReturn(ResponseEntity.ok().build());
        ExpressWifiException exception = Assertions.assertThrows(ExpressWifiException.class, () -> {
            paymentService.requestENkapPayment(paymentRequest);
        });
        assertEquals(ExpessWifiStatus.FAILURE, exception.getErrorResponse().getExpessWifiStatus());
        assertEquals(105, exception.getErrorResponse().getError().getCode());
    }

    @Test
    @DisplayName("Test Enkap Payment Request Payment Id Unauthorized")
    void requestENkapPaymentPaymentIdUnauthorized() {

        ResponseEntity<String> responseEntity = ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        //mock enkap order request
        when(restTemplate.postForEntity(any(String.class), any(ENkapOrderRequest.class), any(Class.class)))
                .thenReturn(responseEntity);
        //mock set up url request
        Mockito.when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), any(Class.class)))
                .thenReturn(ResponseEntity.ok().build());
        ExpressWifiException exception = Assertions.assertThrows(ExpressWifiException.class, () -> {
            paymentService.requestENkapPayment(paymentRequest);
        });
        assertEquals(ExpessWifiStatus.FAILURE, exception.getErrorResponse().getExpessWifiStatus());
        assertEquals(107, exception.getErrorResponse().getError().getCode());
    }

    @Test
    @DisplayName("Test Invalid Hmac")
    void invalidHmacTest() {
        paymentRequest.setHmac("Invalid Hmac");
        //mock set up url request
        Mockito.when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), any(Class.class)))
                .thenReturn(ResponseEntity.ok().build());
        ExpressWifiException exception = Assertions.assertThrows(ExpressWifiException.class, () -> {
            paymentService.requestENkapPayment(paymentRequest);
        });
        assertEquals(ExpessWifiStatus.FAILURE, exception.getErrorResponse().getExpessWifiStatus());
        assertEquals(102, exception.getErrorResponse().getError().getCode());
    }

    @Test
    @DisplayName("Test Successful ENkap Status Request")
    void requestStatusFromEnkapSucessful() {
        String enkapStatusResponse = "{\"status\": \"CONFIRMED\"}";

        ResponseEntity<String> responseEntity = ResponseEntity.status(HttpStatus.OK)
                .body(enkapStatusResponse);
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        //mock enkap order request
        when(restTemplate.postForEntity(any(String.class), any(), any(Class.class), argumentCaptor.capture()
        )).thenReturn(responseEntity);

        StatusResponse statusResponse = paymentService.requestStatusFromENkap(statusRequest);

        assertEquals(ExpessWifiStatus.SUCCESS, statusResponse.getExpessWifiStatus());
        assertEquals(statusRequest.getPaymentId(),argumentCaptor.getValue());
    }

    @Test
    @DisplayName("Test Status Request Unauthorized")
    void requestStatusFromEnkapNotFound() {

        ResponseEntity<String> responseEntity = ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        //mock enkap order request
        when(restTemplate.postForEntity(any(String.class), any(), any(Class.class), argumentCaptor.capture()
        )).thenReturn(responseEntity);
        ExpressWifiException expressWifiException = assertThrows(ExpressWifiException.class, () -> {
            paymentService.requestStatusFromENkap(statusRequest);
        });

        assertEquals(105, expressWifiException.getErrorResponse().getError().getCode());
        assertEquals(ExpessWifiStatus.FAILURE, expressWifiException.getErrorResponse().getExpessWifiStatus());
        assertEquals(statusRequest.getPaymentId(), argumentCaptor.getValue());
    }

    @Test
    void requestStatusFromEnkapUnauthorized() {
        ResponseEntity<String> responseEntity = ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        //mock enkap order request
        when(restTemplate.postForEntity(any(String.class), any(), any(Class.class), argumentCaptor.capture()
        )).thenReturn(responseEntity);
        ExpressWifiException expressWifiException = assertThrows(ExpressWifiException.class, () -> {
            paymentService.requestStatusFromENkap(statusRequest);
        });

        assertEquals(107, expressWifiException.getErrorResponse().getError().getCode());
        assertEquals(ExpessWifiStatus.FAILURE, expressWifiException.getErrorResponse().getExpessWifiStatus());
        assertEquals(statusRequest.getPaymentId(), argumentCaptor.getValue());
    }


    @Configuration
    @ComponentScan(basePackageClasses = {PaymentServiceImpl.class})
    static class Config {
    }


}