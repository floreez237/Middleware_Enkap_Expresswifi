package com.maviance.middleware_enkap_expresswifi.config;

import com.maviance.middleware_enkap_expresswifi.service.interfaces.AuthenticationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Collections;

@Configuration
@Slf4j
public class RestConfig {


    private final AuthenticationService authenticationService;

    public RestConfig(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Bean
    public RestTemplate restTemplate() {
        final RestTemplate restTemplate = new RestTemplate();
        SimpleClientHttpRequestFactory requestFactory = (SimpleClientHttpRequestFactory) restTemplate.getRequestFactory();
        requestFactory.setReadTimeout(7000);
        requestFactory.setConnectTimeout(7000);
        restTemplate.getInterceptors().add((request, body, execution) -> {
            if (!request.getHeaders().containsKey("Authorization")) {//for all requests except Generating Token Request
                request.getHeaders().put("Accept", Collections.singletonList(MediaType.APPLICATION_JSON_VALUE));
                request.getHeaders().put("Content-Type", Collections.singletonList(MediaType.APPLICATION_JSON_VALUE));
                request.getHeaders().put("Connection", Collections.singletonList("keep-alive"));

                String token = "Bearer ".concat(authenticationService.generateToken());
                request.getHeaders().add("Authorization", token);
            }
            String message =
                    "\n\n--> " + request.getMethodValue() + ' ' + request.getURI().toURL().toString() + "\n Headers: " +
                            request.getHeaders() + "\nBody:\n " + new String(body) + "\n-->END " + request.getMethodValue() + " Request\n";
            log.debug(message);
//            System.out.println(message);
            return execution.execute(request, body);
        });
        return restTemplate;
    }
}
