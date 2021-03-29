package com.maviance.middleware_enkap_expresswifi.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maviance.middleware_enkap_expresswifi.enums.ExpessWifiStatus;
import com.maviance.middleware_enkap_expresswifi.enums.RequestSource;
import com.maviance.middleware_enkap_expresswifi.model.request.ExpressWifiPaymentRequest;
import com.maviance.middleware_enkap_expresswifi.model.request.ExpressWifiStatusRequest;
import com.maviance.middleware_enkap_expresswifi.model.response.RedirectResponse;
import com.maviance.middleware_enkap_expresswifi.utils.JsonMapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class PaymentControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;


    @Test
    void initiatePayment() throws Exception {
        ExpressWifiPaymentRequest expressWifiPaymentRequest = new ExpressWifiPaymentRequest(100.0, "XAF",
                "f9d64e122dad630f15afeb8da7ace194950423b741153e4397b1ca94f7705518", 123454684, RandomStringUtils.randomAlphanumeric(10), "https://www.florian.com", "69822334", RequestSource.CUSTOMER);
        final String jsonResponse = mockMvc.perform(post("/v1/payment").contentType(MediaType.APPLICATION_JSON)
                .content(JsonMapper.objectToJson(expressWifiPaymentRequest, expressWifiPaymentRequest.getClass())))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();
        RedirectResponse redirectResponse = JsonMapper.jsonToObject(jsonResponse, RedirectResponse.class);

        assertEquals(ExpessWifiStatus.SUCCESS,redirectResponse.getExpessWifiStatus());
        assertNotNull(redirectResponse.getRedirectUrl());
        assertNotEquals(0,redirectResponse.getRedirectUrl().length());
    }

    @Test
    void getStatus() throws Exception {
        ExpressWifiStatusRequest statusRequest = new ExpressWifiStatusRequest("1PVrLat7No", "f9d64e122dad630f15afeb8da7ace194950423b741153e4397b1ca94f7705518", "PART0001", 123454684);
        final String jsonResponse = mockMvc.perform(MockMvcRequestBuilders.post("/v1/status").contentType(MediaType.APPLICATION_JSON)
                .content(JsonMapper.objectToJson(statusRequest, statusRequest.getClass())))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();
        System.out.println(jsonResponse);
    }
}