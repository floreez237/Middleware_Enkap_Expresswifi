package com.maviance.middleware_enkap_expresswifi.bootstrap;

import com.maviance.middleware_enkap_expresswifi.enums.ExpressWifiStatus;
import com.maviance.middleware_enkap_expresswifi.exceptions.ExpressWifiException;
import com.maviance.middleware_enkap_expresswifi.model.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@SuppressWarnings("DuplicatedCode")
@Component
@Slf4j
public class SetupUrlBootstrap implements CommandLineRunner {
    private final RestTemplate restTemplate;

    @Value("${enkap.setup.url}")
    private String eNkapSetupUrl;

    @Value("${middleware.domain}")
    private String middlewareDomain;

    public SetupUrlBootstrap(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Value("${express.wifi.secret}")

    @Override
    public void run(String... args) throws Exception {
        setRedirectUrl(middlewareDomain.concat("/v1/redirect"));
    }

    private void setRedirectUrl(String redirectUrl) {
        String jsonRequestBody = "{" +
                "\"notificationUrl\": \"\"," +
                "\"returnUrl\":\"" + redirectUrl + "\"" +
                "}";
        try {
            HttpEntity<String> request = new HttpEntity<>(jsonRequestBody);
            log.debug("Sending Setup Redirect Url Request to ENkap");
            restTemplate.exchange(eNkapSetupUrl, HttpMethod.PUT, request, String.class);
            log.info("Successfully Set Enkap Return URL");
        } catch (HttpClientErrorException e) {
            String jsonResponse = e.getResponseBodyAsString();
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                log.error("Incorrect Enkap Access Token. Error message body from Enkap: {}", jsonResponse);
                log.debug("Sending Error Response to Express Wifi");
                throw getInternalError("Unable to precess Payment Request");
            }
            log.error("Unexpected Client Error. Error message body from Enkap: {}", jsonResponse);
            log.debug("Sending Error Response to Express Wifi");
            throw getInternalError("Unexpected Error");
        } catch (HttpServerErrorException exception) {
            log.error("Enkap Server Error occurred. Error Message Body: {}", exception.getResponseBodyAsString());
            throw getInternalError("An Internal Error Occurred");
        }


    }

    private ExpressWifiException getInternalError(String body) {
        ErrorResponse.ErrorObject errorObject = new ErrorResponse.ErrorObject(body, "InternalException", 500, "ADWEDsda12sd_JwR");
        ErrorResponse response = new ErrorResponse(errorObject);
        response.setExpressWifiStatus(ExpressWifiStatus.FAILURE);
        return new ExpressWifiException(response);
    }
}
