package com.maviance.middleware_enkap_expresswifi.service.implementations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maviance.middleware_enkap_expresswifi.service.interfaces.AuthenticationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${enkap.generation.token}")
    private String generationToken;

    public AuthenticationServiceImpl() {
        restTemplate.getInterceptors().add((request, body, execution) -> {
            String message =
                    "\n\n--> " + request.getMethodValue() + ' ' + request.getURI().toURL().toString() + "\n Headers: " +
                            request.getHeaders() + "\nBody:\n " + new String(body) + "\n-->END " + request.getMethodValue() + " Request\n";
            log.debug(message);
            return execution.execute(request, body);
        });
    }

    @Override
    public String generateToken() {
        String authorization = "Basic ".concat(generationToken);
        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.add("grant_type","client_credentials");
        RequestEntity<MultiValueMap<String, String>> requestEntity = RequestEntity.post("https://api.enkap.cm/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", authorization)
                .body(multiValueMap);
        String jsonBody = restTemplate.exchange(requestEntity, String.class)
                .getBody();
        try {
            JsonNode jsonNode = new ObjectMapper().readTree(jsonBody);
            log.debug("Token Generated");
            return jsonNode.get("access_token").asText();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
