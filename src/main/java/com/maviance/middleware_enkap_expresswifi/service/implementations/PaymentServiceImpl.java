package com.maviance.middleware_enkap_expresswifi.service.implementations;

import com.maviance.middleware_enkap_expresswifi.enums.ExpessWifiStatus;
import com.maviance.middleware_enkap_expresswifi.exceptions.ExpressWifiException;
import com.maviance.middleware_enkap_expresswifi.model.request.ENkapOrderRequest;
import com.maviance.middleware_enkap_expresswifi.model.request.ExpressWifiPaymentRequest;
import com.maviance.middleware_enkap_expresswifi.model.request.ExpressWifiStatusRequest;
import com.maviance.middleware_enkap_expresswifi.model.response.*;
import com.maviance.middleware_enkap_expresswifi.service.interfaces.PaymentService;
import com.maviance.middleware_enkap_expresswifi.utils.CryptoUtils;
import com.maviance.middleware_enkap_expresswifi.utils.JsonMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;

@Service
@Slf4j
public class PaymentServiceImpl implements PaymentService {
    private final RestTemplate restTemplate;
    @Value("${enkap.order.url}")
    private String eNkapPlaceOrderUrl;
    @Value("${enkap.status.url}")
    private String eNkapGetStatusUrl;
    @Value("${express.wifi.secret}")
    private String expressWifiSecret;
    @Value("${express.wifi.id}")
    private String expressWifiId;
    @Value("${enkap.version}")
    private String eNkapVersion;
    @Value("${enkap.setup.url}")
    private String eNkapSetupUrl;

    public PaymentServiceImpl(RestTemplate restTemplate) {

        this.restTemplate = restTemplate;
    }

    @Override
    public RedirectResponse requestENkapPayment(ExpressWifiPaymentRequest expressWifiPaymentRequest) {
        verifyHmac(expressWifiPaymentRequest.getHmac(),expressWifiPaymentRequest.getTimestamp(),expressWifiId);
        ENkapOrderRequest eNkapOrderRequest = mapExpressWifiToEnkapRequest(expressWifiPaymentRequest);
        log.debug("Sending Payment Request to Enkap");
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(eNkapPlaceOrderUrl, eNkapOrderRequest, String.class);
        log.debug("Enkap Payment Response");
        String jsonResponse = responseEntity.getBody();

        if (responseEntity.getStatusCode() == HttpStatus.CREATED) {
            log.info("Successful Enkap Payment Request");
            ENkapOrderResponse eNkapOrderResponse = JsonMapper.jsonToObject(jsonResponse, ENkapOrderResponse.class);
            RedirectResponse redirectResponse = new RedirectResponse();
            redirectResponse.setRedirectUrl(eNkapOrderResponse.getRedirectUrl());
            redirectResponse.setExpessWifiStatus(ExpessWifiStatus.SUCCESS);
            log.debug("Sending Response to Express Wifi");
            return redirectResponse;
        } else if (responseEntity.getStatusCode() == HttpStatus.NOT_FOUND) {
            log.error("Invalid PaymentId from ExpressWifi");
            ErrorResponse.ErrorObject errorObject = new ErrorResponse.ErrorObject("Incorrect PaymentId", "XWFApiException", 105, "AZ1BKlO7qIdTD7yyBUA_JwR");
            ErrorResponse response = new ErrorResponse(errorObject);
            response.setExpessWifiStatus(ExpessWifiStatus.FAILURE);
            log.debug("Sending Error Response to Express Wifi");
            throw new ExpressWifiException(response);
        } else if (responseEntity.getStatusCode() == HttpStatus.UNAUTHORIZED) {
            log.error("Incorrect Enkap Access Token");
            log.debug("Sending Error Response to Express Wifi");
            throw getInternalError(jsonResponse);
        }else{
            log.error("Internal Error From Enkap");
            log.debug("Sending Error Response to Express Wifi");
            throw getInternalError(jsonResponse);
        }
    }

    @Override
    public StatusResponse requestStatusFromENkap(ExpressWifiStatusRequest expressWifiStatusRequest) {
        verifyHmac(expressWifiStatusRequest.getHmac(),expressWifiStatusRequest.getTimestamp(),expressWifiStatusRequest.getPartnerId());
        log.debug("Sending Status Request to Enkap");
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(eNkapGetStatusUrl.concat("?orderMerchantId={merchantId}"), String.class, expressWifiStatusRequest.getPaymentId());
        log.debug("Enkap Status Response");
        String jsonBody = responseEntity.getBody();
        ErrorResponse.ErrorObject errorObject;
        ErrorResponse response;
        switch (responseEntity.getStatusCode()) {
            case OK:
                log.info("Successfully retrieved Order Status from ENkap");
                ENkapStatusResponse eNkapStatusResponse = JsonMapper.jsonToObject(jsonBody, ENkapStatusResponse.class);
                return mapEnkapToExpressWifiStatusResponse(eNkapStatusResponse);
            case NOT_FOUND:
                log.error("Incorrect PaymentId from Express Wifi");
                errorObject = new ErrorResponse.ErrorObject("Incorrect PaymentId", "XWFApiException",
                        105, "AZ1BKlO7qIdTD7yyBUA_JwR");
                response = new ErrorResponse(errorObject);
                response.setExpessWifiStatus(ExpessWifiStatus.FAILURE);
                log.debug("Sending Error Response to Express Wifi");
                throw new ExpressWifiException(response);
            case UNAUTHORIZED:
                log.error("Incorrect Enkap Access Token");
                log.error("Sending Error Response to Express Wifi");
                throw getInternalError(jsonBody);
            default:
                log.error("Enkap Internal Error");
                log.error("Sending Error Response to Express Wifi");
                throw getInternalError(jsonBody);
        }

    }

    private StatusResponse mapEnkapToExpressWifiStatusResponse(ENkapStatusResponse eNkapStatusResponse) {
        ExpessWifiStatus expessWifiStatus;
        switch (eNkapStatusResponse.getStatus()) {
            case FAILED:
                expessWifiStatus = ExpessWifiStatus.ERROR;
                break;
            case CANCELED:
                expessWifiStatus = ExpessWifiStatus.CANCELED;
                break;
            case CONFIRMED:
                expessWifiStatus = ExpessWifiStatus.SUCCESS;
                break;
            default:
                expessWifiStatus = ExpessWifiStatus.PENDING;
        }
        log.error("Invalid Status from ENkap");
        log.debug("Sending Error Response to Express Wifi");
        return new StatusResponse(expessWifiStatus);
    }

    private ENkapOrderRequest mapExpressWifiToEnkapRequest(ExpressWifiPaymentRequest expressWifiPaymentRequest) {
        log.debug("Mapping Express Wifi Request to Enkap Request");
        ENkapOrderRequest eNkapOrderRequest = new ENkapOrderRequest().withCurrency(expressWifiPaymentRequest.getCurrency())
                .withItemList(new ArrayList<>()).withMerchantReference(expressWifiPaymentRequest.getPaymentId())
                .withOrderDate(Instant.now().toString()).withPhoneNumber(expressWifiPaymentRequest.getPhoneNumber())
                .withReceiptUrl(expressWifiPaymentRequest.getCallbackUrl()).withDescription("Data");
        //to set return url in enkap system
        setRedirectUrl(expressWifiPaymentRequest.getCallbackUrl());
        ENkapOrderRequest.Item item = new ENkapOrderRequest.Item().withItemId(expressWifiPaymentRequest.getPaymentId())
                .withParticulars("Data Pack").withQuantity(1).withUnitCost(expressWifiPaymentRequest.getAmount());
        item.calculateSubtotal();

        eNkapOrderRequest.addItem(item);

        ENkapOrderRequest.CustomId customId = new ENkapOrderRequest.CustomId(UUID.randomUUID().toString(), eNkapVersion);
        eNkapOrderRequest.setCustomId(customId);
        eNkapOrderRequest.calculateTotalAmount();
        log.debug("Mapped Express Wifi Request to Enkap Request");
        return eNkapOrderRequest;
    }



    private void verifyHmac(String hmacToCheck,long timestamp,String expressWifiPartnerId) {
        String toEncode = timestamp + expressWifiPartnerId;
        String hmac = CryptoUtils.generateHMAC(toEncode, expressWifiSecret);
        if (!hmac.equals(hmacToCheck)) {
            log.error("Hmacs doe not match");
            ErrorResponse.ErrorObject errorObject = new ErrorResponse.ErrorObject("The HMAC is Invalid",
                    "ExpressWifiException", 102, "AQtaCsj1ueMx6u3B3a8zHTR");
            ErrorResponse errorResponse = new ErrorResponse(errorObject);
            errorResponse.setExpessWifiStatus(ExpessWifiStatus.FAILURE);
            log.debug("Sending Error Response to Express Wifi");
            throw new ExpressWifiException(errorResponse);
        }
    }

    private void setRedirectUrl(String redirectUrl) {
        String jsonRequestBody = "{" +
                "\"notificationUrl\": \"\"," +
                "\"returnUrl\":\""+redirectUrl+"\"" +
                "}";
        HttpEntity<String> request = new HttpEntity<>(jsonRequestBody);
        log.debug("Sending Setup Redirect Url Request to ENkap");
        ResponseEntity<String> responseEntity = restTemplate.exchange(eNkapSetupUrl, HttpMethod.PUT, request, String.class);
        log.debug("Received Enkap Setup Redirect Url Response");
        switch (responseEntity.getStatusCode()) {
            case OK:
                log.info("Successfully Set Enkap Return URL");
                return;
            case UNAUTHORIZED:
                log.error("Invalid Access Token");
                log.error("Sending Error Response to Express Wifi");
                throw getInternalError(responseEntity.getBody());
            default:
                log.error("Enkap Internal Error");
                log.error("Sending Error Response to Express Wifi");
                throw getInternalError(responseEntity.getBody());
        }

    }

    private ExpressWifiException getInternalError(String body) {
        ErrorResponse.ErrorObject errorObject = new ErrorResponse.ErrorObject(body, "InternalException", 107, "ADWEDsda12sd_JwR");
        ErrorResponse response = new ErrorResponse(errorObject);
        response.setExpessWifiStatus(ExpessWifiStatus.FAILURE);
        return new ExpressWifiException(response);
    }

}
