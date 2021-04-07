package com.maviance.middleware_enkap_expresswifi.service.implementations;

import com.maviance.middleware_enkap_expresswifi.enums.ExpressWifiStatus;
import com.maviance.middleware_enkap_expresswifi.exceptions.ExpressWifiException;
import com.maviance.middleware_enkap_expresswifi.model.request.ENkapOrderRequest;
import com.maviance.middleware_enkap_expresswifi.model.request.ExpressWifiPaymentRequest;
import com.maviance.middleware_enkap_expresswifi.model.request.ExpressWifiStatusRequest;
import com.maviance.middleware_enkap_expresswifi.model.request.MiddleWareRequestEntity;
import com.maviance.middleware_enkap_expresswifi.model.response.*;
import com.maviance.middleware_enkap_expresswifi.repositories.RequestRepository;
import com.maviance.middleware_enkap_expresswifi.service.interfaces.PaymentService;
import com.maviance.middleware_enkap_expresswifi.utils.CryptoUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;

/**
 * This class is the Primary Implementation of the {@link PaymentService}
 *
 * @author Florian Lowe
 */
@Service
@Slf4j
public class PaymentServiceImpl implements PaymentService {
    private final RestTemplate restTemplate;
    private final RequestRepository requestRepository;
    @Value("${enkap.order.url}")
    private String eNkapPlaceOrderUrl;
    @Value("${enkap.status.url}")
    private String eNkapStatusUrl;
    @Value("${express.wifi.secret}")
    private String expressWifiSecret;
    @Value("${express.wifi.id}")
    private String expressWifiId;
    @Value("${enkap.version}")
    private String eNkapVersion;
    @Value("${enkap.setup.url}")
    private String eNkapSetupUrl;

    public PaymentServiceImpl(RestTemplate restTemplate, RequestRepository requestRepository) {
        this.restTemplate = restTemplate;
        this.requestRepository = requestRepository;
    }

    /**
     * This Method is used to request a Payment from E-Nkap. The {@link ENkapOrderResponse Enkap response}
     * sent back from E-Nkap is then reformatted into a {@link RedirectResponse Redirect Response} that is returned.
     *
     * @param expressWifiPaymentRequest This object contains all the required attributes from Express Wifi's Payment Request.
     * @return RedirectResponse
     * @throws ExpressWifiException This Exception is thrown when any error occurs.
     */
    @Override
    public RedirectResponse requestENkapPayment(ExpressWifiPaymentRequest expressWifiPaymentRequest) {
        verifyHmac(expressWifiPaymentRequest.getHmac(), expressWifiPaymentRequest.getTimestamp(), expressWifiId);
        //creating request entity to save in DB
        MiddleWareRequestEntity middleWareRequestEntity = new MiddleWareRequestEntity();
        middleWareRequestEntity.configureWithEWPaymentRequest(expressWifiPaymentRequest);
        log.info("Saving Request to DB...");
        requestRepository.save(middleWareRequestEntity);
        //The Express Wifi Request is mapped into an Enkap Order Request.
        ENkapOrderRequest eNkapOrderRequest = mapExpressWifiToEnkapRequest(expressWifiPaymentRequest);
        try {
            log.debug("Sending Payment Request to Enkap");
            ENkapOrderResponse response = restTemplate.postForObject(eNkapPlaceOrderUrl, eNkapOrderRequest, ENkapOrderResponse.class);
            assert response != null;
            RedirectResponse redirectResponse = new RedirectResponse();
            redirectResponse.setExpressWifiStatus(ExpressWifiStatus.SUCCESS);
            redirectResponse.setRedirectUrl(response.getRedirectUrl());
            log.debug("Enkap Payment Response");
            return redirectResponse;
        } catch (HttpClientErrorException exception) {
            String jsonResponse = exception.getResponseBodyAsString();
            if (exception.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.error("Invalid PaymentId from ExpressWifi");
                ErrorResponse.ErrorObject errorObject = new ErrorResponse.ErrorObject("Incorrect PaymentId", "XWFApiException", 105, "AZ1BKlO7qIdTD7yyBUA_JwR");
                ErrorResponse response = new ErrorResponse(errorObject);
                response.setExpressWifiStatus(ExpressWifiStatus.FAILURE);
                middleWareRequestEntity.setStatus(ExpressWifiStatus.FAILURE);
                log.debug("Sending Error Response to Express Wifi");
                throw new ExpressWifiException(response);
            } else if (exception.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                log.error("Incorrect Enkap Access Token. Error message body from Enkap: {}", jsonResponse);
                log.debug("Sending Error Response to Express Wifi");
                middleWareRequestEntity.setStatus(ExpressWifiStatus.DECLINED);
                throw getInternalError("Unable to precess Payment Request");
            } else {
                log.error("Unexpected Client Error. Error message body from Enkap: {}", jsonResponse);
                log.debug("Sending Error Response to Express Wifi");
                middleWareRequestEntity.setStatus(ExpressWifiStatus.FAILURE);
                throw getInternalError("Unexpected Error");
            }
        } catch (HttpServerErrorException exception) {
            log.error("Enkap Server Error occurred. Error Message Body: {}", exception.getResponseBodyAsString());
            middleWareRequestEntity.setStatus(ExpressWifiStatus.FAILURE);
            throw getInternalError("An Internal Error Occurred");
        } finally {
            requestRepository.save(middleWareRequestEntity);
        }
    }


    /**
     * This Method is used to request a Transaction Status from E-Nkap. The {@link ENkapStatusResponse Enkap Status response}
     * sent back from E-Nkap is then reformatted into a {@link StatusResponse Express Wifi Status Response} that is returned.
     *
     * @param expressWifiStatusRequest This object contains all the required attributes from Express Wifi's Status Request.
     * @return StatusResponse
     * @throws ExpressWifiException This Exception is thrown when any error occurs.
     */
    @Override
    public StatusResponse requestStatusFromENkap(ExpressWifiStatusRequest expressWifiStatusRequest) {
        verifyHmac(expressWifiStatusRequest.getHmac(), expressWifiStatusRequest.getTimestamp(), expressWifiStatusRequest.getPartnerId());

        try {
            log.debug("Sending Status Request to Enkap");
            ENkapStatusResponse enkapStatusResponse = restTemplate.getForObject(eNkapStatusUrl.concat("?orderMerchantId={merchantId}"), ENkapStatusResponse.class, expressWifiStatusRequest.getPaymentId());
            log.info("Successfully retrieved Order Status from ENkap");
            assert enkapStatusResponse != null;
            final StatusResponse statusResponse = mapEnkapToExpressWifiStatusResponse(enkapStatusResponse);
            MiddleWareRequestEntity middleWareRequestEntity = requestRepository.findById(expressWifiStatusRequest.getPaymentId()).orElseThrow(() -> {
                log.error("Payment Id not found in DB");
                return new RuntimeException("Payment Id not found in DB");
            });
            middleWareRequestEntity.setStatus(statusResponse.getStatus());
            //changing request status in db
            requestRepository.save(middleWareRequestEntity);
            return statusResponse;
        } catch (HttpClientErrorException e) {
            String jsonBody = e.getResponseBodyAsString();
            ErrorResponse.ErrorObject errorObject;
            ErrorResponse response;
            switch (e.getStatusCode()) {
                case NOT_FOUND:
                    log.error("Incorrect PaymentId from Express Wifi");
                    errorObject = new ErrorResponse.ErrorObject("Incorrect PaymentId", "XWFApiException",
                            105, "AZ1BKlO7qIdTD7yyBUA_JwR");
                    response = new ErrorResponse(errorObject);
                    response.setExpressWifiStatus(ExpressWifiStatus.FAILURE);
                    log.debug("Sending Error Response to Express Wifi");
                    throw new ExpressWifiException(response);
                case UNAUTHORIZED:
                    log.error("Incorrect Enkap Access Token. Error message body from Enkap: {}", jsonBody);
                    log.debug("Sending Error Response to Express Wifi");
                    throw getInternalError("Unable to precess Payment Request");
                default:
                    log.error("Unexpected Client Error. Error message body from Enkap: {}", jsonBody);
                    log.debug("Sending Error Response to Express Wifi");
                    throw getInternalError("Unexpected Error");
            }
        } catch (HttpServerErrorException exception) {
            log.error("Enkap Server Error occurred. Error Message Body: {}",exception.getResponseBodyAsString());
            throw getInternalError("An Internal Error Occurred");
        }
    }

    private StatusResponse mapEnkapToExpressWifiStatusResponse(ENkapStatusResponse eNkapStatusResponse) {
        ExpressWifiStatus expressWifiStatus;
        switch (eNkapStatusResponse.getStatus()) {
            case FAILED:
                expressWifiStatus = ExpressWifiStatus.ERROR;
                break;
            case CANCELED:
                expressWifiStatus = ExpressWifiStatus.CANCELED;
                break;
            case CONFIRMED:
                expressWifiStatus = ExpressWifiStatus.SUCCESS;
                break;
            default:
                expressWifiStatus = ExpressWifiStatus.PENDING;
        }
        return new StatusResponse(expressWifiStatus);
    }

    private ENkapOrderRequest mapExpressWifiToEnkapRequest(ExpressWifiPaymentRequest expressWifiPaymentRequest) {
        log.debug("Mapping Express Wifi Request to Enkap Request");
        ENkapOrderRequest eNkapOrderRequest = new ENkapOrderRequest().withCurrency(expressWifiPaymentRequest.getCurrency())
                .withItemList(new ArrayList<>()).withMerchantReference(expressWifiPaymentRequest.getPaymentId())
                .withOrderDate(Instant.now().toString()).withPhoneNumber(expressWifiPaymentRequest.getPhoneNumber())
                .withReceiptUrl(expressWifiPaymentRequest.getCallbackUrl()).withDescription("Data");
        /*//to set return url in enkap system
        setRedirectUrl(expressWifiPaymentRequest.getCallbackUrl());*/
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
            errorResponse.setExpressWifiStatus(ExpressWifiStatus.FAILURE);
            log.debug("Sending Error Response to Express Wifi");
            throw new ExpressWifiException(errorResponse);
        }
    }

    private ExpressWifiException getInternalError(String body) {
        ErrorResponse.ErrorObject errorObject = new ErrorResponse.ErrorObject(body, "InternalException", 500, "ADWEDsda12sd_JwR");
        ErrorResponse response = new ErrorResponse(errorObject);
        response.setExpressWifiStatus(ExpressWifiStatus.FAILURE);
        return new ExpressWifiException(response);
    }

}
