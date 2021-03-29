package com.maviance.middleware_enkap_expresswifi.service.implementations;

import com.maviance.middleware_enkap_expresswifi.config.RestConfig;
import com.maviance.middleware_enkap_expresswifi.service.interfaces.AuthenticationService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
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

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes ={RestConfig.class,PaymentServiceIT.Config.class})
@TestPropertySource(locations = {"classpath:application.properties"})
public class PaymentServiceIT {
    @Autowired
    RestTemplate restTemplate;
    @Value("${enkap.setup.url}")
    private String eNkapSetupUrl;

    @Test
    @DisplayName("Setup Url Request Test")
    void testSetupUrl() {

        String redirectUrl = "https://www.florian.com";
        String jsonRequestBody = "{" +
                "\"notificationUrl\": \"\"," +
                "\"returnUrl\":\""+redirectUrl+"\"" +
                "}";
        HttpEntity<String> request = new HttpEntity<>(jsonRequestBody);

        ResponseEntity<String> responseEntity = restTemplate.exchange(eNkapSetupUrl, HttpMethod.PUT, request, String.class);
        Assertions.assertEquals(HttpStatus.OK,responseEntity.getStatusCode());

    }

    @Configuration
    @ComponentScan(basePackageClasses = AuthenticationServiceImpl.class)
    static class Config {
    }
}
