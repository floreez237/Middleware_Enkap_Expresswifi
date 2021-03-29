package com.maviance.middleware_enkap_expresswifi.service.interfaces;

import com.maviance.middleware_enkap_expresswifi.model.request.ExpressWifiPaymentRequest;
import com.maviance.middleware_enkap_expresswifi.model.request.ExpressWifiStatusRequest;
import com.maviance.middleware_enkap_expresswifi.model.response.RedirectResponse;
import com.maviance.middleware_enkap_expresswifi.model.response.StatusResponse;

public interface PaymentService {
    RedirectResponse requestENkapPayment(ExpressWifiPaymentRequest expressWifiPaymentRequest);

    StatusResponse requestStatusFromENkap(ExpressWifiStatusRequest expressWifiStatusRequest);
}
