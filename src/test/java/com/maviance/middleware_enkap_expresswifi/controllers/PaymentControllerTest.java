package com.maviance.middleware_enkap_expresswifi.controllers;

import com.maviance.middleware_enkap_expresswifi.enums.RequestSource;
import com.maviance.middleware_enkap_expresswifi.model.request.ExpressWifiPaymentRequest;
import com.maviance.middleware_enkap_expresswifi.model.request.ExpressWifiStatusRequest;
import com.maviance.middleware_enkap_expresswifi.model.response.RedirectResponse;
import com.maviance.middleware_enkap_expresswifi.service.interfaces.PaymentService;
import com.maviance.middleware_enkap_expresswifi.utils.JsonMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.regex.Matcher;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest
class PaymentControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    PaymentService paymentService;

    @Test
    @DisplayName("Test Invalid Payment Request Body")
    void initiatePaymentInvalidBody() throws Exception {
        ExpressWifiPaymentRequest expressWifiPaymentRequest = new ExpressWifiPaymentRequest(100.0, "XAF",
                "sdsd", 4545, "zxzx", "http://www.florian.com", "2323", RequestSource.CUSTOMER);
        RedirectResponse redirectResponse = new RedirectResponse();
        Mockito.when(paymentService.requestENkapPayment(expressWifiPaymentRequest))
                .thenReturn(redirectResponse);
        mockMvc.perform(post("/v1/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonMapper.objectToJson(expressWifiPaymentRequest, expressWifiPaymentRequest.getClass())))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("failure"))
                .andExpect(jsonPath("$.error.code").value(106));
    }

    @Test
    @DisplayName("Test status request with Invalid Body")
    void getStatusWithInvalidBody() throws Exception {
        ExpressWifiStatusRequest statusRequest = new ExpressWifiStatusRequest("", "sds", "asa", 454545545L);
        mockMvc.perform(post("v1/status").contentType(MediaType.APPLICATION_JSON)
                .content(JsonMapper.objectToJson(statusRequest, statusRequest.getClass())))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("failure"))
                .andExpect(jsonPath("$.error.code").value(102));


    }
}