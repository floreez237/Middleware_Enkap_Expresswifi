package com.maviance.middleware_enkap_expresswifi.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maviance.middleware_enkap_expresswifi.enums.ExpressWifiStatus;
import com.maviance.middleware_enkap_expresswifi.enums.RequestSource;
import com.maviance.middleware_enkap_expresswifi.model.request.ExpressWifiPaymentRequest;
import com.maviance.middleware_enkap_expresswifi.model.request.ExpressWifiStatusRequest;
import com.maviance.middleware_enkap_expresswifi.model.request.MiddleWareRequestEntity;
import com.maviance.middleware_enkap_expresswifi.model.response.RedirectResponse;
import com.maviance.middleware_enkap_expresswifi.repositories.RequestRepository;
import com.maviance.middleware_enkap_expresswifi.utils.JsonMapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PaymentControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    RequestRepository requestRepository;


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

        assertEquals(ExpressWifiStatus.SUCCESS, redirectResponse.getExpressWifiStatus());
        assertNotNull(redirectResponse.getRedirectUrl());
        assertNotEquals(0,redirectResponse.getRedirectUrl().length());
    }

    @Test
    void getStatus() throws Exception {
        final String requestPaymentId = "1PVrLat7No";
        MiddleWareRequestEntity requestEntity = new MiddleWareRequestEntity().withAmount(14.0)
                .withCurrency("xaf").withExpressWifiCallBackUrl("sdsdsd")
                .withOrderDate(new Date()).withPhoneNumber("343423").withRequestPaymentId(requestPaymentId)
                .withStatus(ExpressWifiStatus.PENDING);
        requestRepository.save(requestEntity);
        ExpressWifiStatusRequest statusRequest = new ExpressWifiStatusRequest(requestPaymentId, "f9d64e122dad630f15afeb8da7ace194950423b741153e4397b1ca94f7705518", "PART0001", 123454684);

        final String jsonResponse = mockMvc.perform(MockMvcRequestBuilders.post("/v1/status").contentType(MediaType.APPLICATION_JSON)
                .content(JsonMapper.objectToJson(statusRequest, statusRequest.getClass())))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();
        System.out.println(jsonResponse);
        MiddleWareRequestEntity updatedRequestEntity = requestRepository.findById(requestPaymentId).orElse(null);
        assert updatedRequestEntity != null;
        assertEquals(ExpressWifiStatus.PENDING, updatedRequestEntity.getStatus());
    }
}