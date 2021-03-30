package com.maviance.middleware_enkap_expresswifi.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/v1")
@Slf4j
public class ViewController {
    /*@GetMapping(value = {"/redirect", "/redirect/"})
    public String redirectToExpressWifi(@RequestParam Map<String, String> queryParametersMap) {
        String paymentId = queryParametersMap.get("orderMerchantId");
        if (paymentId == null) {
            log.error("Request does not contain the orderMerchantId parameter");
            throw new ParameterNotFoundException("orderMerchantId must be present");
        }
        return "redirect:".concat(callBackObject.getCallbackUrl());
    }*/
}
