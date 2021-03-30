package com.maviance.middleware_enkap_expresswifi.controllers;

import com.maviance.middleware_enkap_expresswifi.model.request.ExpressWifiPaymentRequest;
import com.maviance.middleware_enkap_expresswifi.model.request.ExpressWifiStatusRequest;
import com.maviance.middleware_enkap_expresswifi.model.response.RedirectResponse;
import com.maviance.middleware_enkap_expresswifi.model.response.StatusResponse;
import com.maviance.middleware_enkap_expresswifi.service.interfaces.PaymentService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/v1")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/payment")
    public RedirectResponse initiatePayment(@RequestBody @Valid ExpressWifiPaymentRequest expressWifiPaymentRequest) {
        return paymentService.requestENkapPayment(expressWifiPaymentRequest);
    }

    @PostMapping("/status")
    public StatusResponse getStatus(@RequestBody @Valid ExpressWifiStatusRequest expressWifiStatusRequest) {
        return paymentService.requestStatusFromENkap(expressWifiStatusRequest);
    }


}

