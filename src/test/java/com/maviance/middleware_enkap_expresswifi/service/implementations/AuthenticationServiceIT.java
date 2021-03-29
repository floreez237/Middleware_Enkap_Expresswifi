package com.maviance.middleware_enkap_expresswifi.service.implementations;

import com.maviance.middleware_enkap_expresswifi.config.RestConfig;
import com.maviance.middleware_enkap_expresswifi.service.interfaces.AuthenticationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@TestPropertySource(properties = {"enkap.generation.token=MGVsSkZEdU5ZMmxhVlRNZ1hqVzR6UHpYYmVFYTpZYWFKOEdpRHpoSHBlaksyV04wTWp1N2VFREVh"})
@ContextConfiguration(classes = {RestConfig.class,AuthenticationServiceIT.config.class})
class AuthenticationServiceIT {

    @Autowired
    private AuthenticationService authenticationService;

    @Test
    @DisplayName("Generate Token Test")
    void generateToken() {
        String token = authenticationService.generateToken();
        System.out.println(token);
        assertNotEquals(0, token.length());
    }

    @Configuration
    @ComponentScan(basePackageClasses = AuthenticationServiceImpl.class)
    static class config {
    }

}