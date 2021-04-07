package com.maviance.middleware_enkap_expresswifi.controllers;

import com.maviance.middleware_enkap_expresswifi.exceptions.PaymentIdNotFoundException;
import com.maviance.middleware_enkap_expresswifi.model.request.MiddleWareRequestEntity;
import com.maviance.middleware_enkap_expresswifi.repositories.RequestRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/v1")
@Slf4j
public class ViewController {
    private final RequestRepository requestRepository;

    public ViewController(RequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

    //do not change this endpoint
    @GetMapping(value = {"/redirect/{paymentId}"})
    public String redirectToExpressWifi(@PathVariable String paymentId) {
        MiddleWareRequestEntity middleWareRequestEntity = requestRepository.findById(paymentId).orElse(null);
        if (middleWareRequestEntity == null) {
            log.error("Payment Id Not Found in DB");
            throw new PaymentIdNotFoundException();
        }
        return "redirect:".concat(middleWareRequestEntity.getExpressWifiCallBackUrl());
    }

    @ExceptionHandler(PaymentIdNotFoundException.class)
    @ResponseStatus
    @ResponseBody
    public String handlePaymentIdNotFound() {
        return "ERROR";
    }
}
